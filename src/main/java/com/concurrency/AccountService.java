package com.concurrency;

import com.concurrency.util.DistributedLock;
import lombok.RequiredArgsConstructor;
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

    @DistributedLock(key = "'deposit'.concat(':').concat(#id)")
    public Long deposit(Long id, Long amount) {
        Account account = repository.findByIdWithPessimisticLock(id)
                .orElseThrow();
        account.deposit(amount);
        return account.getBalance();
    }

    @Transactional
    public Long withdraw(Long id, Long amount) {
        Account account = repository.findByIdWithPessimisticLock(id).orElseThrow();
        account.withdraw(amount);
        return account.getBalance();
    }
}