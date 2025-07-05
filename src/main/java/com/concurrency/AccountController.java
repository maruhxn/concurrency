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
    public Long deposit(@PathVariable Long id, @RequestBody Long amount) {
        return accountService.deposit(id, amount);
    }

    @PostMapping("/{id}/withdraw")
    public Long withdraw(@PathVariable Long id, @RequestBody Long amount) {
        return accountService.withdraw(id, amount);
    }
}
