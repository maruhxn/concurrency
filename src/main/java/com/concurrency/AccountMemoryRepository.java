package com.concurrency;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class AccountMemoryRepository implements AccountRepository {

    private final ConcurrentHashMap<Long, Account> store = new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        store.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<Account> findById(Long id) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        Account account = store.get(id);
        return Optional.ofNullable(account);
    }

    @Override
    public void deleteAll() {
        store.clear();
    }
}
