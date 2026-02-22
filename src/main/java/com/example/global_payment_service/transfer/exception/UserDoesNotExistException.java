package com.example.global_payment_service.transfer.exception;

import java.util.UUID;

public class UserDoesNotExistException extends BadTransferRequestException {
    public UserDoesNotExistException(UUID userId) {
        super(String.format("User with id='%s' does not exist", userId));
    }
}
