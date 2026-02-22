package com.example.global_payment_service.transfer.monitor.api;

import com.example.global_payment_service.account.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferViewDto(UUID senderId, UUID recipientId, BigDecimal amount, Currency currency) {
}
