package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Account;
import com.example.global_payment_service.account.AccountRepository;
import com.example.global_payment_service.account.Currency;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
        var idempotencyKey = UUID.randomUUID();
        var transfer = new Transfer(idempotencyKey, sender, recipient, BigDecimal.ONE, Currency.EUR);

        // when
        var saved = transferRepository.save(transfer);

        // then
        assertEquals(idempotencyKey, saved.getIdempotencyKey());
        assertEquals(sender, saved.getSender());
        assertEquals(recipient, saved.getRecipient());
        assertEquals(BigDecimal.ONE, saved.getBalance());
        assertEquals(Currency.EUR, saved.getCurrency());
    }

    @Test
    void shouldThrowOnIdempotencyKeyConflict() {
        // given
        var sender = accountRepository.save(new Account(new BigDecimal(100), Currency.EUR));
        var recipient = accountRepository.save(new Account(new BigDecimal(50), Currency.EUR));
        var idempotencyKey = UUID.randomUUID();
        var transfer = new Transfer(idempotencyKey, sender, recipient, BigDecimal.ONE, Currency.EUR);
        transferRepository.saveAndFlush(transfer);

        // when
        assertThrows(DataIntegrityViolationException.class, () -> transferRepository.saveAndFlush(new Transfer(idempotencyKey, sender, recipient, BigDecimal.TWO, Currency.HUF)));
    }

}