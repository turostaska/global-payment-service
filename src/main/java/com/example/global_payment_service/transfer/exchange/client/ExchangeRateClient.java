package com.example.global_payment_service.transfer.exchange.client;

import com.example.global_payment_service.account.Currency;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public interface ExchangeRateClient {
    @NonNull
    BigDecimal getExchangeRate(@NonNull Currency from, @NonNull Currency to);
}
