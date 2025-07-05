package com.concurrency;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;
    private Long balance;

    public Account(Long balance) {
        this.balance = balance;
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public void withdraw(Long amount) {
        long newBalance = this.getBalance() - amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.balance = newBalance;
    }

}
