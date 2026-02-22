package com.example.global_payment_service.transfer.api;

import com.example.global_payment_service.transfer.TransferOrchestratorService;
import com.example.global_payment_service.transfer.TransferService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/transfers")
public class TransferController {

    private final TransferOrchestratorService transferOrchestratorService;

    public TransferController(TransferOrchestratorService transferOrchestratorService) {
        this.transferOrchestratorService = transferOrchestratorService;
    }

    @PostMapping
    ResponseEntity<Void> transfer(
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
            @RequestBody TransferRequestDto transferData
    ) {
        var status = transferOrchestratorService.handleTransferRequest(idempotencyKey, transferData);
        return switch (status) {
            case COMPLETED -> ResponseEntity.status(HttpStatusCode.valueOf(201)).build();
            case PROCESSING -> ResponseEntity.status(HttpStatusCode.valueOf(409)).build();
            case BAD_REQUEST -> ResponseEntity.badRequest().build();
            case FAILED -> ResponseEntity.status(HttpStatusCode.valueOf(503)).build();
        };
    }

}
