package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Balance;
import com.example.global_payment_service.transfer.api.TransferRequestDto;
import com.example.global_payment_service.transfer.exception.BadTransferRequestException;
import com.example.global_payment_service.transfer.exception.InsufficientFundsException;
import com.example.global_payment_service.transfer.exception.NegativeTransferException;
import com.example.global_payment_service.transfer.idempotency.IdempotencyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Log4j2
@Service
public class TransferOrchestratorService {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;

    public TransferOrchestratorService(TransferService transferService, IdempotencyService idempotencyService) {
        this.transferService = transferService;
        this.idempotencyService = idempotencyService;
    }

    public TransferStatus handleTransferRequest(UUID idempotencyKey, TransferRequestDto request) {
        try {
            idempotencyService.reserve(idempotencyKey);
        } catch (DataIntegrityViolationException e) {
            return idempotencyService.getStatus(idempotencyKey).orElseThrow();
        }

        try {
            transferService.transfer(request.from(), request.to(), new Balance(request.amount(), request.currency()));
            idempotencyService.updateStatus(idempotencyKey, TransferStatus.COMPLETED);
            return TransferStatus.COMPLETED;
        } catch (BadTransferRequestException e) {
            idempotencyService.updateStatus(idempotencyKey, TransferStatus.BAD_REQUEST);
            return TransferStatus.BAD_REQUEST;
        } catch (Exception e) {
            log.debug(() -> String.format("Failed to transfer %s %s from %s to %s", request.amount(), request.currency(), request.from(), request.to()), e);
            idempotencyService.updateStatus(idempotencyKey, TransferStatus.FAILED);
            throw e;
        }
    }

}
