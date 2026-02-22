package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Account;
import com.example.global_payment_service.account.AccountRepository;
import com.example.global_payment_service.account.Balance;
import com.example.global_payment_service.transfer.exception.InsufficientFundsException;
import com.example.global_payment_service.transfer.exception.NegativeTransferException;
import com.example.global_payment_service.transfer.exception.UserDoesNotExistException;
import com.example.global_payment_service.transfer.exchange.ExchangeService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferValidatorService {
    private final AccountRepository accountRepository;
    private final ExchangeService exchangeService;

    public TransferValidatorService(AccountRepository accountRepository, ExchangeService exchangeService) {
        this.accountRepository = accountRepository;
        this.exchangeService = exchangeService;
    }

    @Transactional()
    public TransferValidationResult validate(UUID fromId, UUID toId, Balance balance) {
        if (balance.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeTransferException("negative transfer");
        }

        var from = validateAccountExists(fromId);
        var to = validateAccountExists(toId);

        var balanceToDeduct = exchangeService.exchangeTo(balance, from.getCurrency());
        if (balanceToDeduct.amount().compareTo(from.getBalance()) > 0) {
            throw new InsufficientFundsException("insufficient funds");
        }

        var balanceToAdd = exchangeService.exchangeTo(balance, to.getCurrency());

        return new TransferValidationResult(from, to, balanceToDeduct, balanceToAdd);
    }

    public record TransferValidationResult(Account from, Account to, Balance balanceToDeduct, Balance balanceToAdd) {
    }

    private @NonNull Account validateAccountExists(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new UserDoesNotExistException(id));
    }
}
