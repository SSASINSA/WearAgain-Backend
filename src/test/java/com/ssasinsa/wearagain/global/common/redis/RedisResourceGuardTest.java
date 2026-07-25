package com.ssasinsa.wearagain.global.common.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RedisResourceGuardTest {

    private RedisResourceGuard resourceGuard;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        resourceGuard = new RedisResourceGuard();
        executor = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void should_allow_concurrent_readers_when_resource_is_same() throws Exception {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(1L);

        try (RedisResourceGuard.LockHandle ignored = resourceGuard.acquireRead(resourceKey)) {
            Future<Boolean> secondReader = executor.submit(() -> {
                try (RedisResourceGuard.LockHandle ignoredSecond = resourceGuard.acquireRead(resourceKey)) {
                    return true;
                }
            });

            assertThat(secondReader.get(1, TimeUnit.SECONDS)).isTrue();
            assertThat(trackedLocks()).hasSize(1);
        }
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_wait_for_reader_when_writer_acquires_same_resource() throws Exception {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(2L);
        CountDownLatch writerStarted = new CountDownLatch(1);

        try (RedisResourceGuard.LockHandle reader = resourceGuard.acquireRead(resourceKey)) {
            ReentrantReadWriteLock lock = trackedLock(resourceKey);
            Future<Boolean> writer = executor.submit(() -> {
                writerStarted.countDown();
                Optional<RedisResourceGuard.LockHandle> acquired =
                        resourceGuard.tryAcquireWrite(resourceKey, Duration.ofSeconds(2));
                if (acquired.isEmpty()) {
                    return false;
                }
                try (RedisResourceGuard.LockHandle ignored = acquired.get()) {
                    return true;
                }
            });

            assertThat(writerStarted.await(1, TimeUnit.SECONDS)).isTrue();
            awaitQueuedThread(lock);

            reader.close();
            assertThat(writer.get(1, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_keep_lock_entry_while_writer_is_waiting() throws Exception {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(3L);
        CountDownLatch writerAcquired = new CountDownLatch(1);
        CountDownLatch releaseWriter = new CountDownLatch(1);

        RedisResourceGuard.LockHandle reader = resourceGuard.acquireRead(resourceKey);
        ReentrantReadWriteLock lock = trackedLock(resourceKey);
        Future<Boolean> writer = executor.submit(() -> {
            Optional<RedisResourceGuard.LockHandle> acquired =
                    resourceGuard.tryAcquireWrite(resourceKey, Duration.ofSeconds(2));
            if (acquired.isEmpty()) {
                return false;
            }
            try (RedisResourceGuard.LockHandle ignored = acquired.get()) {
                writerAcquired.countDown();
                return releaseWriter.await(5, TimeUnit.SECONDS);
            }
        });

        awaitQueuedThread(lock);
        reader.close();
        assertThat(writerAcquired.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(lock.hasQueuedThreads()).isFalse();

        Future<Boolean> nextReader = executor.submit(() -> {
            try (RedisResourceGuard.LockHandle ignored = resourceGuard.acquireRead(resourceKey)) {
                return true;
            }
        });
        awaitQueuedThread(lock);
        assertThat(nextReader).isNotDone();

        releaseWriter.countDown();
        assertThat(writer.get(1, TimeUnit.SECONDS)).isTrue();
        assertThat(nextReader.get(1, TimeUnit.SECONDS)).isTrue();
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_not_block_when_resource_types_are_different() throws Exception {
        RedisResourceKey firstResource = RedisResourceKey.storeItem(4L);
        RedisResourceKey secondResource = RedisResourceKey.eventOption(4L);

        try (RedisResourceGuard.LockHandle ignored = resourceGuard.acquireRead(firstResource)) {
            Optional<RedisResourceGuard.LockHandle> second =
                    resourceGuard.tryAcquireWrite(secondResource, Duration.ZERO);

            assertThat(second).isPresent();
            second.orElseThrow().close();
        }
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_return_empty_when_write_lock_times_out() throws Exception {
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(5L);

        try (RedisResourceGuard.LockHandle ignored = resourceGuard.acquireRead(resourceKey)) {
            Optional<RedisResourceGuard.LockHandle> acquired =
                    resourceGuard.tryAcquireWrite(resourceKey, Duration.ofMillis(50));

            assertThat(acquired).isEmpty();
            assertThat(trackedLocks()).hasSize(1);
        }
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_preserve_interrupt_when_write_lock_wait_is_interrupted() throws Exception {
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(6L);
        CountDownLatch writerStarted = new CountDownLatch(1);
        CountDownLatch writerFinished = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);

        try (RedisResourceGuard.LockHandle ignored = resourceGuard.acquireRead(resourceKey)) {
            executor.execute(() -> {
                writerStarted.countDown();
                try {
                    resourceGuard.tryAcquireWrite(resourceKey, Duration.ofSeconds(10));
                } catch (InterruptedException exception) {
                    interrupted.set(true);
                    Thread.currentThread().interrupt();
                } finally {
                    writerFinished.countDown();
                }
            });

            assertThat(writerStarted.await(1, TimeUnit.SECONDS)).isTrue();
            executor.shutdownNow();
            assertThat(writerFinished.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(interrupted).isTrue();
            assertThat(trackedLocks()).hasSize(1);
        }
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_reject_close_when_thread_does_not_own_lock() throws Exception {
        RedisResourceGuard.LockHandle lockHandle =
                resourceGuard.acquireRead(RedisResourceKey.storeItem(7L));

        Future<RuntimeException> wrongThreadClose = executor.submit(() -> {
            try {
                lockHandle.close();
                return null;
            } catch (RuntimeException exception) {
                return exception;
            }
        });

        assertThat(wrongThreadClose.get(1, TimeUnit.SECONDS))
                .isInstanceOf(IllegalMonitorStateException.class);

        lockHandle.close();
        Optional<RedisResourceGuard.LockHandle> writeLock = resourceGuard.tryAcquireWrite(
                RedisResourceKey.storeItem(7L),
                Duration.ZERO
        );
        assertThat(writeLock).isPresent();
        writeLock.orElseThrow().close();
        assertThat(trackedLocks()).isEmpty();
    }

    @Test
    void should_not_accumulate_entries_after_resources_are_released() {
        for (long resourceId = 1; resourceId <= 1_000; resourceId++) {
            try (RedisResourceGuard.LockHandle ignored =
                         resourceGuard.acquireRead(RedisResourceKey.storeItem(resourceId))) {
                assertThat(trackedLocks()).hasSize(1);
            }
        }

        assertThat(trackedLocks()).isEmpty();
    }

    private Map<?, ?> trackedLocks() {
        return (Map<?, ?>) ReflectionTestUtils.getField(resourceGuard, "locks");
    }

    private ReentrantReadWriteLock trackedLock(RedisResourceKey resourceKey) {
        Object lockEntry = trackedLocks().get(resourceKey);
        return (ReentrantReadWriteLock) ReflectionTestUtils.getField(lockEntry, "lock");
    }

    private void awaitQueuedThread(ReentrantReadWriteLock lock) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (!lock.hasQueuedThreads() && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        assertThat(lock.hasQueuedThreads()).isTrue();
    }
}
