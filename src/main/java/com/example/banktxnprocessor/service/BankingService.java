package com.example.banktxnprocessor.service;

import com.example.banktxnprocessor.exception.AccountAlreadyExistsException;
import com.example.banktxnprocessor.exception.AccountNotFoundException;
import com.example.banktxnprocessor.exception.InsufficientFundsException;
import com.example.banktxnprocessor.exception.InvalidAmountException;
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
            throw new AccountAlreadyExistsException(id);
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

        LedgerEntry entry = createEntry(
                account, TransactionType.DEPOSIT,
                amount, newBalance, null, null, Instant.now());

        account.updateBalance(newBalance);
        account.addLedgerEntry(entry);
    }

    public synchronized void withdraw(
            String accountId, BigDecimal amount) {

        validateAmount(amount);

        Account account = getAccount(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        BigDecimal newBalance =
                account.getBalance().subtract(amount);

        LedgerEntry entry = createEntry(
                account, TransactionType.WITHDRAWAL,
                amount, newBalance, null, null, Instant.now());

        account.updateBalance(newBalance);
        account.addLedgerEntry(entry);
    }

    public synchronized void transfer(
            String fromId, String toId, BigDecimal amount) {

        validateAmount(amount);

        if (fromId == null || toId == null
                || fromId.isBlank() || toId.isBlank()) {
            throw new IllegalArgumentException(
                    "Source and destination IDs are required");
        }

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException(
                    "Source and destination must differ");
        }

        // Find both accounts before changing either balance.
        Account source = getAccount(fromId);
        Account destination = getAccount(toId);

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        BigDecimal sourceBalance =
                source.getBalance().subtract(amount);

        BigDecimal destinationBalance =
                destination.getBalance().add(amount);

        UUID transferId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // Prepare both ledger entries before changing state.
        LedgerEntry debit = createEntry(
                source, TransactionType.TRANSFER_OUT,
                amount, sourceBalance, transferId,
                toId, timestamp);

        LedgerEntry credit = createEntry(
                destination, TransactionType.TRANSFER_IN,
                amount, destinationBalance, transferId,
                fromId, timestamp);

        // Apply both changes within the synchronized operation.
        source.updateBalance(sourceBalance);
        destination.updateBalance(destinationBalance);

        source.addLedgerEntry(debit);
        destination.addLedgerEntry(credit);
    }

    private Account getAccount(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Account ID must not be blank");
        }

        return repository.findById(id)
                .orElseThrow(
                        () -> new AccountNotFoundException(id));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than zero");
        }

        // This kata assumes amounts are expressed to 2 decimal places.
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidAmountException(
                    "Amount must have at most 2 decimal places");
        }
    }

    private LedgerEntry createEntry(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            UUID transferId,
            String relatedAccountId,
            Instant timestamp) {

        return new LedgerEntry(
                UUID.randomUUID(),
                transferId,
                account.getId(),
                type,
                amount,
                balanceAfter,
                timestamp,
                relatedAccountId);
    }
}
