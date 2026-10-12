package com.example.banktxnprocessor.repository;


import com.example.banktxnprocessor.model.Account;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Repository
public class AccountRepository {

    private final Map<String, Account> accounts =
            new ConcurrentHashMap<>();

    public boolean exists(String id) {
        return accounts.containsKey(id);
    }

    public void save(Account account) {
        accounts.put(account.getId(), account);
    }

    public boolean createIfAbsent(Account account) {
        return accounts.putIfAbsent(account.getId(), account) == null;
    }

    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }
}
