package com.concurrency;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    void deleteAll();

}
