package com.concurrency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    AccountService service;

    @Autowired
    AccountRepository repository;

    @Test
    void 잔액_조회() {
        // given
        Long accountId = 1L;
        repository.save(new Account(accountId, 1000L));

        // when
        Long balance = service.getBalance(accountId);

        // then
        assertThat(balance).isEqualTo(1000L);
    }

    @Test
    void 입금() {
        // given
        Long accountId = 1L;
        repository.save(new Account(accountId, 1000L));


        // when
        Long balance = service.deposit(accountId, 1000L);

        // then
        assertThat(balance).isEqualTo(2000L);
    }

    @Test
    void 출금() {
        // given
        Long accountId = 1L;
        repository.save(new Account(accountId, 1000L));


        // when
        Long balance = service.withdraw(accountId, 1000L);

        // then
        assertThat(balance).isEqualTo(0L);
    }

    @Test
    void 잔액이_출금_금액보다_적을_경우_실패해야_하며_실패() {
        // given
        Long accountId = 1L;
        repository.save(new Account(accountId, 100L));

        // when / then
        assertThatThrownBy(() -> service.withdraw(accountId, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔액이 부족합니다.");
    }
}