package com.example.global_payment_service.transfer.exception;

public abstract class BadTransferRequestException extends RuntimeException {
    public BadTransferRequestException(String message) {
        super(message);
    }
}
