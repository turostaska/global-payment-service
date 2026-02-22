package com.example.global_payment_service.transfer.monitor;

import com.example.global_payment_service.account.Account;
import com.example.global_payment_service.account.Currency;
import com.example.global_payment_service.transfer.Transfer;
import com.example.global_payment_service.transfer.TransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class SuccessfulTransferMonitorServiceTest {

    @MockitoBean
    TransferRepository mockTransferRepository;

    @Autowired
    SuccessfulTransferMonitorService transferMonitorService;

    @Test
    void shouldBeAbleToGetSuccessfulTransfers() {
        // given
        var pageRequest = PageRequest.of(0, 1);
        var transferToReturn = new Transfer(
                UUID.randomUUID(),
                new Account(BigDecimal.TWO, Currency.EUR),
                new Account(BigDecimal.ONE, Currency.EUR),
                BigDecimal.ONE,
                Currency.HUF);
        when(mockTransferRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(transferToReturn)));

        // when
        var page = transferMonitorService.getSuccessfulTransfers(0, 1);

        // then
        var content = page.get().toList();
        assertEquals(1, content.size());
        var transfer = content.getFirst();
        assertEquals(BigDecimal.ONE, transfer.amount());
        assertEquals(Currency.HUF, transfer.currency());
        assertEquals(transferToReturn.getSender().getId(), transfer.senderId());
        assertEquals(transferToReturn.getRecipient().getId(), transfer.recipientId());
    }

}