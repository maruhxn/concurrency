package com.concurrency;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountJpaRepository repository;
    private final RedissonClient redissonClient; // RedissonClient 주입

    @Transactional(readOnly = true)
    public Long getBalance(Long id) {
        Account account = repository.findById(id).orElseThrow();
        return account.getBalance();
    }

    @Transactional
    public Long deposit(Long id, Long amount) {
        String action = "deposit";
        String lockName = "LOCK:" + action + ":" + id;
        RLock lock = redissonClient.getLock(lockName);

        try {
            if (!lock.tryLock()) {
                throw new IllegalStateException("다른 입금 요청이 처리 중입니다.");
            }

            Account account = repository.findByIdWithPessimisticLock(id)
                    .orElseThrow();
            account.deposit(amount);
            return account.getBalance();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public Long withdraw(Long id, Long amount) {
        Account account = repository.findByIdWithPessimisticLock(id).orElseThrow();
        account.withdraw(amount);
        return account.getBalance();
    }
}