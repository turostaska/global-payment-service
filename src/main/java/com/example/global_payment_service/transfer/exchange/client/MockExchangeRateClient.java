package com.example.global_payment_service.transfer.exchange.client;

import com.example.global_payment_service.account.Currency;
import lombok.NonNull;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
public class MockExchangeRateClient implements ExchangeRateClient {

    private static final BigDecimal HUF_PER_EUR = new BigDecimal("379.08");
    private static final BigDecimal HUF_PER_USD = new BigDecimal("322.13");

    @Override
    public @NonNull BigDecimal getExchangeRate(@NonNull Currency from, @NonNull Currency to) {
        var serviceUnavailableException = HttpServerErrorException.create("Service unavailable", HttpStatusCode.valueOf(503), "Service unavailable", null, null, null);
        try {
            // Simulate latency of the API
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw serviceUnavailableException;
        }

        // Simulate "flakiness" of the API
        if (new Random().nextDouble() < 0.1) throw serviceUnavailableException;

        if (from.equals(to)) return BigDecimal.ONE;
        return getExchangeRateToHuf(from).divide(getExchangeRateToHuf(to), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal getExchangeRateToHuf(@NonNull Currency from) {
        return switch (from) {
            case EUR -> HUF_PER_EUR;
            case USD -> HUF_PER_USD;
            case HUF -> BigDecimal.ONE;
        };
    }
}
