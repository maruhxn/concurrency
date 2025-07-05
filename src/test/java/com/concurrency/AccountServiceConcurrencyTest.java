package com.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AccountServiceConcurrencyTest {

    @Autowired
    AccountService service;

    @Autowired
    AccountRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void 입금_요청이_동시에_2개_이상_올_경우_하나_빼고_실패() throws Exception {
        Long accountId = 1L;
        repository.save(new Account(accountId, 0L));
        int n = 50;
        ExecutorService es = Executors.newFixedThreadPool(n);
        CountDownLatch readyThreadCounter = new CountDownLatch(n); // 준비 카운트
        CountDownLatch callingThreadBlocker = new CountDownLatch(1); // 요청 카운트
        CountDownLatch completeThreadCounter = new CountDownLatch(n); // 완료 카운트

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        Runnable task = new WaitingWorker(
                () -> {
                    try {
                        service.deposit(accountId, 1000L);
                        successCount.incrementAndGet();
                    } catch (IllegalStateException e) {
                        failedCount.incrementAndGet();
                    }
                },
                readyThreadCounter,
                callingThreadBlocker,
                completeThreadCounter
        );

        // when
        System.out.println("n개의 작업을 스레드 풀에 제출");
        for (int i = 0; i < n; i++) {
            es.submit(task);
        }

        readyThreadCounter.await();
        callingThreadBlocker.countDown();
        completeThreadCounter.await();
        System.out.println("모든 작업 완료");

        // then
        Account account = repository.findById(accountId).get();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failedCount.get()).isEqualTo(49);
        assertThat(account.getBalance()).isEqualTo(1000L);
    }

    @Test
    void 입금과_출금_요청이_동시에_올_경우_오차없이_처리() throws Exception {
        Long accountId = 2L;
        Long initialBalance = 1000L;
        repository.save(new Account(accountId, initialBalance));

        int n = 2;
        ExecutorService es = Executors.newFixedThreadPool(n);
        CountDownLatch readyThreadCounter = new CountDownLatch(n); // 준비 카운트
        CountDownLatch callingThreadBlocker = new CountDownLatch(1); // 요청 카운트
        CountDownLatch completeThreadCounter = new CountDownLatch(n); // 완료 카운트

        es.submit(new WaitingWorker(
                        () -> service.deposit(accountId, 1000L),
                        readyThreadCounter,
                        callingThreadBlocker,
                        completeThreadCounter
                )
        );

        es.submit(new WaitingWorker(
                        () -> service.withdraw(accountId, 500L),
                        readyThreadCounter,
                        callingThreadBlocker,
                        completeThreadCounter
                )
        );

        System.out.println("2개의 작업을 스레드 풀에 제출");
        readyThreadCounter.await();
        callingThreadBlocker.countDown();
        completeThreadCounter.await();
        System.out.println("모든 작업 완료");

        // then
        Account account = repository.findById(accountId).get();
        assertThat(account.getBalance()).isEqualTo(initialBalance + 500L);
    }

    @Test
    void 입금과_출금_요청이_동시에_여러_개_올_경우_순차적으로_처리() throws Exception {
        Long accountId = 3L;
        long initialBalance = 10000L;
        repository.save(new Account(accountId, initialBalance));

        int n = 10;
        ExecutorService es = Executors.newFixedThreadPool(n);
        CountDownLatch latch = new CountDownLatch(n);
        AtomicLong expectedBalance = new AtomicLong(initialBalance);

        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                es.submit(() -> {
                    try {
                        service.deposit(accountId, 1000L);
                        expectedBalance.addAndGet(1000L);
                    } finally {
                        latch.countDown();
                    }
                });
            } else {
                es.submit(() -> {
                    try {
                        service.withdraw(accountId, 500L);
                        expectedBalance.addAndGet(-500L);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        System.out.println("기대값: " + expectedBalance.get());

        // then
        Account account = repository.findById(accountId).get();
        assertThat(account.getBalance()).isEqualTo(expectedBalance.get());
    }


    static class WaitingWorker implements Runnable {

        private final Runnable action;
        private final CountDownLatch readyThreadCounter;
        private final CountDownLatch callingThreadBlocker;
        private final CountDownLatch completedThreadCounter;

        public WaitingWorker(Runnable action, CountDownLatch readyThreadCounter, CountDownLatch callingThreadBlocker, CountDownLatch completedThreadCounter) {
            this.action = action;
            this.readyThreadCounter = readyThreadCounter;
            this.callingThreadBlocker = callingThreadBlocker;
            this.completedThreadCounter = completedThreadCounter;
        }

        public void run() {
            readyThreadCounter.countDown();
            try {
                callingThreadBlocker.await();
                action.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                completedThreadCounter.countDown();
            }
        }
    }
}
