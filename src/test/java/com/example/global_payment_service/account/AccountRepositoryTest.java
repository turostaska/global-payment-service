package com.example.global_payment_service.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    @Test
    void shouldBeAbleToCreateNewAccount() {
        // given
        var account = new Account(new BigDecimal(100), Currency.EUR);

        // when
        var saved = accountRepository.save(account);

        // then
        assertNotNull(saved.getId());
    }

    @Test
    void shouldBeAbleToGetAccountById() {
        // given
        var account = new Account(new BigDecimal(100), Currency.EUR);
        var id = accountRepository.save(account).getId();

        // when
        var fetched = accountRepository.findById(id).orElseThrow();

        // then
        assertNotNull(fetched.getId());
        assertEquals(fetched.getBalance(), account.getBalance());
        assertEquals(fetched.getCurrency(), account.getCurrency());
    }

}
