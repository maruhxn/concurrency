package com.concurrency;

public record AccountEvent(
        Long accountId,
        Long amount,
        AccountEventType type
) {
    public enum AccountEventType {
        DEPOSIT, WITHDRAW
    }
}
