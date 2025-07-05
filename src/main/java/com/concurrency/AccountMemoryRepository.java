package com.concurrency;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountMemoryRepository implements AccountRepository {

    private final ConcurrentHashMap<Long, Account> store = new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {
        store.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<Account> findById(Long id) {
        Account account = store.get(id);
        return Optional.ofNullable(account);
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void deleteAll() {
        store.clear();
    }
}
