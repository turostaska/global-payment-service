package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Account;
import com.example.global_payment_service.account.AccountRepository;
import com.example.global_payment_service.account.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TransferRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransferRepository transferRepository;

    @Test
    void shouldBeAbleToSaveTransfer() {
        // given
        var sender = accountRepository.save(new Account(new BigDecimal(100), Currency.EUR));
        var recipient = accountRepository.save(new Account(new BigDecimal(50), Currency.EUR));
        var transfer = new Transfer(sender, recipient, BigDecimal.ONE, Currency.EUR);

        // when
        var saved = transferRepository.save(transfer);

        // then
        assertEquals(sender, saved.getSender());
        assertEquals(recipient, saved.getRecipient());
        assertEquals(BigDecimal.ONE, saved.getBalance());
        assertEquals(Currency.EUR, saved.getCurrency());
    }

    @Test
    void shouldBeAbleToQueryTransfersWithPaging() {
        // given
        var sender = accountRepository.save(new Account(new BigDecimal(100), Currency.EUR));
        var recipient = accountRepository.save(new Account(new BigDecimal(50), Currency.EUR));
        transferRepository.saveAllAndFlush(
                List.of(
                        new Transfer(sender, recipient, new BigDecimal(1), Currency.EUR),
                        new Transfer(sender, recipient, new BigDecimal(2), Currency.EUR),
                        new Transfer(sender, recipient, new BigDecimal(3), Currency.EUR),
                        new Transfer(sender, recipient, new BigDecimal(4), Currency.EUR),
                        new Transfer(sender, recipient, new BigDecimal(5), Currency.EUR)
                )
        );

        // when
        var page = transferRepository.findAll(PageRequest.of(0, 4));
        assertEquals(5, page.getTotalElements());
        assertEquals(4, page.get().count());
    }

}