package com.example.banktxnprocessor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AmountRequest(
        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amount
) {}

