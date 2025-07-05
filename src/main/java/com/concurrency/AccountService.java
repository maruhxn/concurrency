package com.concurrency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();
    private Set<Long> depositLocks = ConcurrentHashMap.newKeySet();

    public Long getBalance(Long id) {
        Account account = repository.findById(id).orElseThrow();
        return account.getBalance();
    }

    public Long deposit(Long id, Long amount) {
        // 중복 입금 여부를 체크: 이미 입금 진행 중이면 바로 실패
        if (!depositLocks.add(id)) {
            throw new IllegalStateException("입금 처리 중입니다. id: " + id);
        }

        ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();

        try {
            Account account = repository.findById(id).orElseThrow();
            long newBalance = account.getBalance() + amount;
            repository.save(new Account(id, newBalance));
            return newBalance;
        } finally {
            lock.unlock();
        }
    }

    public Long withdraw(Long id, Long amount) {
        ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();

        try {
            Account account = repository.findById(id).orElseThrow();
            long newBalance = account.getBalance() - amount;
            if (newBalance < 0) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            repository.save(new Account(id, newBalance));
            return newBalance;
        } finally {
            lock.unlock();
        }
    }
}
