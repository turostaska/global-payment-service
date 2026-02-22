package com.example.global_payment_service.account;

import java.math.BigDecimal;

public record Balance(BigDecimal amount, Currency currency) {
}
