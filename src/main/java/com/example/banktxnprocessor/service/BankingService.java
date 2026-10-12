package com.example.banktxnprocessor.service;

import com.example.banktxnprocessor.model.Account;
import com.example.banktxnprocessor.model.LedgerEntry;
import com.example.banktxnprocessor.model.TransactionType;
import com.example.banktxnprocessor.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BankingService {

    private final AccountRepository repository;

    public BankingService(AccountRepository repository) {
        this.repository = repository;
    }

    public synchronized Account createAccount(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Account ID must not be blank");
        }

        Account account = new Account(id);

        if (!repository.createIfAbsent(account)) {
            throw new IllegalArgumentException(
                    "Account already exists: " + id);
        }

        return account;
    }

    public synchronized BigDecimal getBalance(String accountId) {
        return getAccount(accountId).getBalance();
    }

    public synchronized List<LedgerEntry> getHistory(
            String accountId) {
        return getAccount(accountId).getLedger();
    }

    public synchronized void deposit(
            String accountId, BigDecimal amount) {

        validateAmount(amount);
        Account account = getAccount(accountId);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.updateBalance(newBalance);

        record(account, TransactionType.DEPOSIT, amount,
                newBalance, null, null);
    }

    public synchronized void withdraw(
            String accountId, BigDecimal amount) {

        validateAmount(amount);
        Account account = getAccount(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.updateBalance(newBalance);

        record(account, TransactionType.WITHDRAWAL, amount,
                newBalance, null, null);
    }

    public synchronized void transfer(
            String fromId, String toId, BigDecimal amount) {

        validateAmount(amount);

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException(
                    "Source and destination must differ");
        }

        Account source = getAccount(fromId);
        Account destination = getAccount(toId);

        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        // Validate everything before mutating either account.
        BigDecimal sourceBalance =
                source.getBalance().subtract(amount);
        BigDecimal destinationBalance =
                destination.getBalance().add(amount);

        UUID transferId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        source.updateBalance(sourceBalance);
        destination.updateBalance(destinationBalance);

        record(source, TransactionType.TRANSFER_OUT, amount,
                sourceBalance, transferId, toId, timestamp);

        record(destination, TransactionType.TRANSFER_IN, amount,
                destinationBalance, transferId, fromId, timestamp);
    }

    private Account getAccount(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found: " + id));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
    }

    private void record(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            UUID transferId,
            String relatedAccountId) {

        record(account, type, amount, balanceAfter,
                transferId, relatedAccountId, Instant.now());
    }

    private void record(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            UUID transferId,
            String relatedAccountId,
            Instant timestamp) {

        account.addLedgerEntry(new LedgerEntry(
                UUID.randomUUID(),
                transferId,
                account.getId(),
                type,
                amount,
                balanceAfter,
                timestamp,
                relatedAccountId
        ));
    }
}
