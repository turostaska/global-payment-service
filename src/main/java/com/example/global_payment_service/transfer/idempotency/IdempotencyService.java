package com.example.global_payment_service.transfer.idempotency;

import com.example.global_payment_service.transfer.TransferStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    public IdempotencyService(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve(UUID idempotencyKey) {
        idempotencyRepository.saveAndFlush(new Idempotency(idempotencyKey, TransferStatus.PROCESSING));
    }

    @Transactional(readOnly = true)
    public Optional<TransferStatus> getStatus(UUID idempotencyKey) {
        return idempotencyRepository.findByIdempotencyKey(idempotencyKey)
                .map(Idempotency::getStatus);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(UUID idempotencyKey, TransferStatus status) {
        idempotencyRepository.setStatusWhereIdempotencyKey(status, idempotencyKey);
    }

}
