package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Balance;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {
    private final TransferValidatorService validatorService;

    public TransferService(TransferValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Transactional()
    public void transfer(@NonNull UUID fromId, @NonNull UUID toId, @NonNull Balance balance) {
        var validationResult = validatorService.validate(fromId, toId, balance);
        var from = validationResult.from();
        var to = validationResult.to();
        var balanceToDeduct = validationResult.balanceToDeduct();
        var balanceToAdd = validationResult.balanceToAdd();

        from.setBalance(from.getBalance().subtract(balanceToDeduct.amount()));
        to.setBalance(to.getBalance().add(balanceToAdd.amount()));
    }
}
