package com.concurrency;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}/balance")
    public Long balance(@PathVariable Long id) {
        return accountService.getBalance(id);
    }

    @PostMapping("/{id}/deposit")
    public void deposit(@PathVariable Long id, @RequestBody Long amount) {
        accountService.deposit(id, amount);
    }

    @PostMapping("/{id}/withdraw")
    public void withdraw(@PathVariable Long id, @RequestBody Long amount) {
        accountService.withdraw(id, amount);
    }
}
