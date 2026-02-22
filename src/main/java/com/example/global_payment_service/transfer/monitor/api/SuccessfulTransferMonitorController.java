package com.example.global_payment_service.transfer.monitor.api;

import com.example.global_payment_service.transfer.monitor.SuccessfulTransferMonitorService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/transfers")
public class SuccessfulTransferMonitorController {

    private final SuccessfulTransferMonitorService monitorService;

    public SuccessfulTransferMonitorController(SuccessfulTransferMonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping
    public Page<TransferViewDto> getSuccessfulTransfers(
        @RequestParam int page,
        @RequestParam int pageSize
    ) {
        return monitorService.getSuccessfulTransfers(page, pageSize);
    }
}
