package com.example.banktxnprocessor.controller;

import com.example.banktxnprocessor.dto.AmountRequest;
import com.example.banktxnprocessor.model.Account;
import com.example.banktxnprocessor.model.LedgerEntry;
import com.example.banktxnprocessor.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final BankingService service;

    public AccountController(BankingService service) {
        this.service = service;
    }

    @PostMapping("/accounts/{id}")
    public Map<String, String> create(@PathVariable String id) {
        Account account = service.createAccount(id);
        return Map.of("accountId", account.getId());
    }

    @GetMapping("/accounts/{id}/balance")
    public Map<String, BigDecimal> balance(@PathVariable String id) {
        return Map.of("balance", service.getBalance(id));
    }

    @GetMapping("/accounts/{id}/transactions")
    public List<LedgerEntry> history(@PathVariable String id) {
        return service.getHistory(id);
    }

    @PostMapping("/accounts/{id}/deposit")
    public Map<String, String> deposit(
            @PathVariable String id,
            @Valid @RequestBody AmountRequest request) {

        service.deposit(id, request.amount());
        return Map.of("status", "DEPOSIT_COMPLETED");
    }

    @PostMapping("/accounts/{id}/withdraw")
    public Map<String, String> withdraw(
            @PathVariable String id,
            @Valid @RequestBody AmountRequest request) {

        service.withdraw(id, request.amount());
        return Map.of("status", "WITHDRAWAL_COMPLETED");
    }


    public record TransferRequest(
            @jakarta.validation.constraints.NotBlank
            String fromAccountId,

            @jakarta.validation.constraints.NotBlank
            String toAccountId,

            @jakarta.validation.constraints.NotNull
            @jakarta.validation.constraints.DecimalMin(
                    value = "0",
                    inclusive = false)
            java.math.BigDecimal amount
    ) {}


    @PostMapping("/transfers")
    public Map<String, String> transfer(
            @Valid @RequestBody TransferRequest request) {

        service.transfer(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount());

        return Map.of("status", "TRANSFER_COMPLETED");
    }
}

