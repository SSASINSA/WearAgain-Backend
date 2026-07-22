package com.ssasinsa.wearagain.global.common.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class RedisTransactionCallbackRegistrarTest {

    private static final RedisResourceKey STORE_ITEM_KEY = RedisResourceKey.storeItem(1L);

    private RedisTransactionCallbackRegistrar registrar;

    @BeforeEach
    void setUp() {
        registrar = new RedisTransactionCallbackRegistrar();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void should_compensate_before_completion_when_transaction_rolls_back() {
        List<String> executionOrder = new ArrayList<>();

        boolean registered = registrar.registerRollbackCompensation(
                STORE_ITEM_KEY,
                () -> executionOrder.add("compensation"),
                () -> executionOrder.add("completion")
        );
        getRegisteredSynchronization().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertThat(registered).isTrue();
        assertThat(executionOrder).containsExactly("compensation", "completion");
    }

    @Test
    void should_run_action_before_completion_when_transaction_commits() {
        List<String> executionOrder = new ArrayList<>();

        boolean registered = registrar.registerAfterCommit(
                STORE_ITEM_KEY,
                () -> executionOrder.add("action"),
                () -> executionOrder.add("completion")
        );
        TransactionSynchronization synchronization = getRegisteredSynchronization();
        synchronization.afterCommit();
        synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

        assertThat(registered).isTrue();
        assertThat(executionOrder).containsExactly("action", "completion");
    }

    @Test
    void should_skip_compensation_when_transaction_status_is_unknown() {
        List<String> executionOrder = new ArrayList<>();

        boolean registered = registrar.registerRollbackCompensation(
                STORE_ITEM_KEY,
                () -> executionOrder.add("compensation"),
                () -> executionOrder.add("completion")
        );
        getRegisteredSynchronization().afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);

        assertThat(registered).isTrue();
        assertThat(executionOrder).containsExactly("completion");
    }

    @Test
    void should_run_completion_when_redis_action_fails() {
        List<String> executionOrder = new ArrayList<>();

        boolean registered = registrar.registerRollbackCompensation(
                STORE_ITEM_KEY,
                () -> {
                    throw new RuntimeException();
                },
                () -> executionOrder.add("completion")
        );
        getRegisteredSynchronization().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertThat(registered).isTrue();
        assertThat(executionOrder).containsExactly("completion");
    }

    @Test
    void should_allow_immediate_cleanup_when_transaction_synchronization_is_inactive() throws Exception {
        RedisResourceGuard resourceGuard = new RedisResourceGuard();
        RedisResourceGuard.LockHandle readLock = resourceGuard.acquireRead(STORE_ITEM_KEY);
        AtomicBoolean compensated = new AtomicBoolean(false);
        Runnable compensation = () -> compensated.set(true);
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);

        boolean registered = registrar.registerRollbackCompensation(
                STORE_ITEM_KEY,
                compensation,
                readLock::close
        );

        assertThat(registered).isFalse();
        assertThat(compensated).isFalse();

        compensation.run();
        readLock.close();

        assertThat(compensated).isTrue();
        Optional<RedisResourceGuard.LockHandle> writeLock =
                resourceGuard.tryAcquireWrite(STORE_ITEM_KEY, Duration.ZERO);
        assertThat(writeLock).isPresent();
        writeLock.orElseThrow().close();
    }

    private TransactionSynchronization getRegisteredSynchronization() {
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);
        return synchronizations.get(0);
    }
}
