package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Account;
import com.example.global_payment_service.account.AccountRepository;
import com.example.global_payment_service.account.Balance;
import com.example.global_payment_service.account.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldBeAbleToTransfer() {
        // given
        var account0 = accountRepository.save(new Account(new BigDecimal(100), Currency.EUR));
        var account1 = accountRepository.save(new Account(new BigDecimal(50), Currency.EUR));

        // when
        transferService.transfer(account0.getId(), account1.getId(), new Balance(new BigDecimal(75), Currency.EUR));

        // then
        var sender = accountRepository.findById(account0.getId()).orElseThrow();
        var recipient = accountRepository.findById(account1.getId()).orElseThrow();
        assertEquals(new BigDecimal(25), sender.getBalance().stripTrailingZeros());
        assertEquals(new BigDecimal(125), recipient.getBalance().stripTrailingZeros());
    }


}