package com.example.global_payment_service.transfer.exchange;

import com.example.global_payment_service.account.Balance;
import com.example.global_payment_service.account.Currency;
import com.example.global_payment_service.transfer.exchange.client.MockExchangeRateClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeServiceTest {

    @Test
    void shouldBeAbleToExchangeFromEurToHuf() {
        // given
        var balance = new Balance(BigDecimal.ONE, Currency.EUR);
        var exchangeService = new ExchangeService(new MockExchangeRateClient());

        // when
        var exchanged = exchangeService.exchangeTo(balance, Currency.HUF);

        // then
        var expected = new Balance(new BigDecimal("379.08"), Currency.HUF);
        assertEquals(exchanged.amount().stripTrailingZeros(), expected.amount());
        assertEquals(exchanged.currency(), expected.currency());
    }

    @Test
    void shouldBeAbleToExchangeFromHufToEur() {
        // given
        var balance = new Balance(BigDecimal.ONE, Currency.HUF);
        var exchangeService = new ExchangeService(new MockExchangeRateClient());

        // when
        var exchanged = exchangeService.exchangeTo(balance, Currency.EUR);

        // then
        var expected = new Balance(new BigDecimal("0.0026379656"), Currency.EUR);
        assertEquals(exchanged, expected);
    }

}