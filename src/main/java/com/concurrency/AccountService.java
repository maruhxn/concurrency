package com.concurrency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;

    public Long getBalance(Long id) {
        Account account = repository.findById(id).orElseThrow();
        return account.getBalance();
    }

    public Long deposit(Long id, Long amount) {
        Account account = repository.findById(id).orElseThrow();
        long newBalance = account.getBalance() + amount;
        repository.save(new Account(id, newBalance));
        return newBalance;
    }

    public Long withdraw(Long id, Long amount) {
        Account account = repository.findById(id).orElseThrow();
        long newBalance = account.getBalance() - amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        repository.save(new Account(id, newBalance));
        return newBalance;
    }
}
