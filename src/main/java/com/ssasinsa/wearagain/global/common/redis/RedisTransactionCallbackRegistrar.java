package com.ssasinsa.wearagain.global.common.redis;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * DB transaction 결과가 확정된 뒤 실행할 Redis 보상·반영 작업과 lock 해제를 등록한다.
 *
 * Redis와 DB를 하나의 transaction으로 묶는 component가 아니다. Redis를 먼저 선점한 뒤 DB가 rollback되면
 * Redis 값을 보상하고, DB commit 이후에만 실행해야 하는 Redis 작업은 commit 성공 뒤 실행하도록 시점을 맞춘다.
 */
@Slf4j
@Component
public class RedisTransactionCallbackRegistrar {

    /**
     * Redis 선점 이후 DB transaction이 rollback될 때 실행할 보상 작업을 등록한다.
     *
     * rollback이면 {@code compensation → completion} 순서로 실행한다. commit이면 Redis 선점을 유지하고
     * completion만 실행한다. transaction 결과가 UNKNOWN이면 잘못된 보상을 피하기 위해 compensation을 실행하지 않고
     * 복구 필요 log를 남긴 뒤 completion을 실행한다.
     *
     * @param resourceKey callback 대상 resource 식별자
     * @param compensation rollback 확정 후 실행할 Redis 반환 작업
     * @param completion transaction 결과와 관계없이 마지막에 실행할 lock 해제 작업
     * @return callback 등록 성공 여부. false이면 어떤 callback도 실행되지 않으므로 호출자가 즉시 보상하고 lock을 해제한다.
     */
    public boolean registerRollbackCompensation(
            RedisResourceKey resourceKey,
            Runnable compensation,
            Runnable completion
    ) {
        Objects.requireNonNull(resourceKey);
        Objects.requireNonNull(compensation);
        Objects.requireNonNull(completion);

        return register(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    if (status == STATUS_ROLLED_BACK) {
                        executeRollbackCompensation(resourceKey, compensation);
                    } else if (status == STATUS_UNKNOWN) {
                        logUnknownCompletion(resourceKey);
                    }
                } finally {
                    executeCompletion(resourceKey, completion);
                }
            }
        });
    }

    /**
     * DB commit이 성공한 뒤에만 실행할 Redis 작업을 등록한다.
     *
     * 주문 취소·행사 신청 취소처럼 DB 상태가 실제로 변경된 뒤 Redis 값을 반환해야 하는 경로에서 사용한다.
     * commit이면 {@code action → completion} 순서로 실행하고 rollback이면 action 없이 completion만 실행한다.
     *
     * @param resourceKey callback 대상 resource 식별자
     * @param action commit 성공 뒤 실행할 Redis 반환·재설정 작업
     * @param completion transaction 결과와 관계없이 마지막에 실행할 lock 해제 작업
     * @return callback 등록 성공 여부. false이면 어떤 callback도 실행되지 않으므로 호출자가 lock을 직접 해제한다.
     */
    public boolean registerAfterCommit(
            RedisResourceKey resourceKey,
            Runnable action,
            Runnable completion
    ) {
        Objects.requireNonNull(resourceKey);
        Objects.requireNonNull(action);
        Objects.requireNonNull(completion);

        return register(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executeAfterCommit(resourceKey, action);
            }

            @Override
            public void afterCompletion(int status) {
                executeCompletion(resourceKey, completion);
            }
        });
    }

    /**
     * DB transaction 결과와 관계없이 종료 후 실행할 작업 등록 메서드.
     */
    public boolean registerCompletion(RedisResourceKey resourceKey, Runnable completion) {
        Objects.requireNonNull(resourceKey);
        Objects.requireNonNull(completion);

        return register(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                executeCompletion(resourceKey, completion);
            }
        });
    }

    /**
     * 현재 DB transaction에 callback 등록 메서드.
     */
    private boolean register(TransactionSynchronization synchronization) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }

        try {
            TransactionSynchronizationManager.registerSynchronization(synchronization);
            return true;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    /**
     * DB rollback 시 Redis 선점 결과 보상 메서드.
     */
    private void executeRollbackCompensation(RedisResourceKey resourceKey, Runnable compensation) {
        try {
            compensation.run();
        } catch (RuntimeException exception) {
            log.error(
                    "[RedisTransactionCallback] rollback compensation 실행에 실패했습니다. resourceType={}, resourceId={}",
                    resourceKey.type(),
                    resourceKey.resourceId(),
                    exception
            );
        }
    }

    /**
     * DB commit 성공 후 Redis 후속 작업 실행 메서드.
     */
    private void executeAfterCommit(RedisResourceKey resourceKey, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            log.error(
                    "[RedisTransactionCallback] after commit 작업에 실패했습니다. resourceType={}, resourceId={}",
                    resourceKey.type(),
                    resourceKey.resourceId(),
                    exception
            );
        }
    }

    /**
     * DB transaction 종료 후 lock 해제 등 마무리 작업 실행 메서드.
     */
    private void executeCompletion(RedisResourceKey resourceKey, Runnable completion) {
        try {
            completion.run();
        } catch (RuntimeException exception) {
            log.error(
                    "[RedisTransactionCallback] completion 작업에 실패했습니다. resourceType={}, resourceId={}",
                    resourceKey.type(),
                    resourceKey.resourceId(),
                    exception
            );
        }
    }

    /**
     * DB transaction 결과 불명 시 Redis 보상 생략 로그 기록 메서드.
     */
    private void logUnknownCompletion(RedisResourceKey resourceKey) {
        log.error(
                "[RedisTransactionCallback] resourceType={}, resourceId={}, transactionStatus=UNKNOWN",
                resourceKey.type(),
                resourceKey.resourceId()
        );
    }
}
