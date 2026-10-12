package com.example.banktxnprocessor.model;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Account {

    private final String id;
    private BigDecimal balance = BigDecimal.ZERO;
    private final List<LedgerEntry> ledger = new ArrayList<>();

    public Account(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<LedgerEntry> getLedger() {
        return List.copyOf(ledger);
    }

    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    public void addLedgerEntry(LedgerEntry entry) {
        ledger.add(entry);
    }
}
