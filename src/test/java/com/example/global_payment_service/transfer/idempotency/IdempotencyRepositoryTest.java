package com.example.global_payment_service.transfer.idempotency;

import com.example.global_payment_service.transfer.TransferStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IdempotencyRepositoryTest {

    @Autowired
    IdempotencyRepository idempotencyRepository;

    @Test
    void shouldBeAbleToSetStatus() {
        // given
        var idempotency = idempotencyRepository.save(new Idempotency(UUID.randomUUID(), TransferStatus.PROCESSING));

        // when
        idempotencyRepository.setStatusWhereIdempotencyKey(TransferStatus.COMPLETED, idempotency.getIdempotencyKey());

        // then
        var fetched = idempotencyRepository.findById(idempotency.getId()).orElseThrow();
        assertEquals(TransferStatus.COMPLETED, fetched.getStatus());
    }

}