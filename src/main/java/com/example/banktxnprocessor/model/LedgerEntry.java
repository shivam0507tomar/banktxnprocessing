package com.example.banktxnprocessor.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID transactionId,
        UUID transferId,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant timestamp,
        String relatedAccountId
) {
    public LedgerEntry(
            UUID transactionId,
            UUID transferId,
            String accountId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            Instant timestamp,
            String relatedAccountId
    ) {
        this.transactionId = transactionId;
        this.transferId = transferId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
        this.relatedAccountId = relatedAccountId;
    }
}

