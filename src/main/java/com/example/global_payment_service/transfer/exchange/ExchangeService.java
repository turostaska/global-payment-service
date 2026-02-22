package com.example.global_payment_service.transfer.exchange;

import com.example.global_payment_service.account.Balance;
import com.example.global_payment_service.account.Currency;
import com.example.global_payment_service.transfer.exchange.client.ExchangeRateClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExchangeService {

    private final ExchangeRateClient exchangeRateClient;

    public ExchangeService(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public @NonNull Balance exchangeTo(@NonNull Balance balance, @NonNull Currency to) {
        var exchangeRate = exchangeRateClient.getExchangeRate(balance.currency(), to);
        var amount = exchangeRate.multiply(balance.amount());
        return new Balance(amount, to);
    }

    private @NonNull BigDecimal getExchangeRate(@NonNull Currency from, @NonNull Currency to) {
        return exchangeRateClient.getExchangeRate(from, to);
    }
}
