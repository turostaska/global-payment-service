package com.example.global_payment_service.transfer.api;

import com.example.global_payment_service.account.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDto(UUID from, UUID to, BigDecimal amount, Currency currency) {
}
