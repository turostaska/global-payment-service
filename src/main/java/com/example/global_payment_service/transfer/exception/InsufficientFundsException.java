package com.example.global_payment_service.transfer.exception;

public class InsufficientFundsException extends BadTransferRequestException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
