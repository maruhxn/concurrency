package com.concurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Account {

    private Long id;
    private Long balance;

    public Account(Long id, Long balance) {
        this.id = id;
        this.balance = balance;
    }
}
