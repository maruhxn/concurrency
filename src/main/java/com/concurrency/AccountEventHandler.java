package com.concurrency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventHandler {

    private final AccountJpaRepository repository;

    @EventListener
    @Transactional
    public void handle(AccountEvent event) {
        try {
            Account account = repository.findByIdWithPessimisticLock(event.accountId())
                    .orElseThrow();

            if (event.type() == AccountEvent.AccountEventType.DEPOSIT) {
                account.deposit(event.amount());
                log.info("DEPOSIT 완료: accountId={}, amount={}", event.accountId(), event.amount());
            } else if (event.type() == AccountEvent.AccountEventType.WITHDRAW) {
                account.withdraw(event.amount());
                log.info("WITHDRAW 완료: accountId={}, amount={}", event.accountId(), event.amount());
            }
        } catch (Exception e) {
            log.error("AccountEvent 처리 실패", e);
            // 실패 시 재시도, DLQ 등 구현 가능
        }
    }
}
