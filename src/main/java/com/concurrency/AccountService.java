package com.concurrency;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountJpaRepository repository;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public Long getBalance(Long id) {
        Account account = repository.findById(id).orElseThrow();
        return account.getBalance();
    }

    public void deposit(Long id, Long amount) {
        publisher.publishEvent(new AccountEvent(id, amount, AccountEvent.AccountEventType.DEPOSIT));
    }

    @Transactional
    public void withdraw(Long id, Long amount) {
        publisher.publishEvent(new AccountEvent(id, amount, AccountEvent.AccountEventType.WITHDRAW));
    }
}