package com.example.banktxnprocessor.exception;

public class InsufficientFundsException
        extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}