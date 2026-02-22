package com.example.global_payment_service.transfer.exception;

public class NegativeTransferException extends BadTransferRequestException {
    public NegativeTransferException(String message) {
        super(message);
    }
}
