package com.ssasinsa.wearagain.global.common.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.stereotype.Component;

/**
 * 진행 중인 요청과 DB 기준 Redis 재동기화가 같은 resource에서 충돌하지 않게 하는 JVM local guard다.
 *
 * 일반 주문·신청·취소는 read lock을 공유하므로 서로 직렬화되지 않는다.
 * DB 기준 재동기화와 관리자 절대값 변경은 제한 시간 write lock을 사용한다.
 *
 * 이 lock은 애플리케이션 인스턴스 사이에서 공유되지 않으므로 단일 writer 인스턴스를 전제로 한다.
 */
@Component
public class RedisResourceGuard {

    private final ConcurrentMap<RedisResourceKey, LockEntry> locks = new ConcurrentHashMap<>();

    /**
     * 일반 요청이 사용하는 공유 lock을 획득한다.
     * 반환된 handle은 DB transaction completion까지 유지한 뒤 획득한 thread에서 닫아야 한다.
     */
    public LockHandle acquireRead(RedisResourceKey resourceKey) {
        LockEntry lockEntry = retainLock(resourceKey);
        Lock lock = lockEntry.lock.readLock();
        boolean acquired = false;
        try {
            lock.lock();
            acquired = true;
            return new LockHandle(this, resourceKey, lockEntry, lock);
        } finally {
            if (!acquired) {
                releaseLock(resourceKey, lockEntry);
            }
        }
    }

    /**
     * 5분 DB 재동기화가 사용하는 제한 시간 write lock 획득 방식이다.
     *
     * 제한 시간 안에 획득하지 못하면 빈 값을 반환하므로 scheduler는 해당 resource만 건너뛰고
     * 다음 주기에 다시 시도할 수 있다. 대기 중 interrupt가 발생하면 호출자가 interrupt 상태를 복구하고
     * 현재 동기화 작업을 중단할 수 있도록 {@link InterruptedException}을 그대로 전달한다.
     */
    public Optional<LockHandle> tryAcquireWrite(RedisResourceKey resourceKey, Duration timeout)
            throws InterruptedException {
        LockEntry lockEntry = retainLock(resourceKey);
        Lock lock = lockEntry.lock.writeLock();
        boolean acquired = false;
        try {
            acquired = lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
            if (!acquired) {
                return Optional.empty();
            }
            return Optional.of(new LockHandle(this, resourceKey, lockEntry, lock));
        } finally {
            if (!acquired) {
                releaseLock(resourceKey, lockEntry);
            }
        }
    }

    /**
     * resource lock 참조 획득 메서드.
     */
    private LockEntry retainLock(RedisResourceKey resourceKey) {
        return locks.compute(resourceKey, (ignored, current) -> {
            LockEntry lockEntry = current == null ? new LockEntry() : current;
            lockEntry.referenceCount++;
            return lockEntry;
        });
    }

    /**
     * resource lock 참조 반환 및 미사용 entry 제거 메서드.
     */
    private void releaseLock(RedisResourceKey resourceKey, LockEntry expected) {
        locks.computeIfPresent(resourceKey, (ignored, current) -> {
            if (current != expected) {
                return current;
            }
            current.referenceCount--;
            return current.referenceCount == 0 ? null : current;
        });
    }

    private static final class LockEntry {

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        private int referenceCount;
    }

    /**
     * 획득한 lock의 소유권을 나타내며 try-with-resources 또는 transaction callback에서 닫는다.
     */
    public static final class LockHandle implements AutoCloseable {

        private final RedisResourceGuard owner;
        private final RedisResourceKey resourceKey;
        private final LockEntry lockEntry;
        private final Lock lock;
        private volatile boolean closed;

        private LockHandle(
                RedisResourceGuard owner,
                RedisResourceKey resourceKey,
                LockEntry lockEntry,
                Lock lock
        ) {
            this.owner = owner;
            this.resourceKey = resourceKey;
            this.lockEntry = lockEntry;
            this.lock = lock;
        }

        /**
         * lock 해제 및 resource 참조 반환 메서드.
         */
        @Override
        public void close() {
            if (closed) {
                return;
            }
            lock.unlock();
            closed = true;
            owner.releaseLock(resourceKey, lockEntry);
        }
    }
}
