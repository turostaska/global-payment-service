package com.example.global_payment_service.transfer.monitor;

import com.example.global_payment_service.transfer.Transfer;
import com.example.global_payment_service.transfer.TransferRepository;
import com.example.global_payment_service.transfer.monitor.api.TransferViewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class SuccessfulTransferMonitorService {

    private final TransferRepository transferRepository;

    public SuccessfulTransferMonitorService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @NonNull
    public Page<TransferViewDto> getSuccessfulTransfers(int page, int pageSize) {
        var pageRequest = PageRequest.of(page, pageSize);
        return transferRepository.findAll(pageRequest).map(this::toDto);
    }

    private TransferViewDto toDto(Transfer transfer) {
        return new TransferViewDto(
                transfer.getSender().getId(),
                transfer.getRecipient().getId(),
                transfer.getBalance(),
                transfer.getCurrency()
        );
    }
}
