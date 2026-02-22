package com.example.global_payment_service.transfer;

import com.example.global_payment_service.account.Account;
import com.example.global_payment_service.account.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transfer {

    public Transfer(UUID idempotencyKey, Account sender, Account recipient, BigDecimal balance, Currency currency) {
        this.idempotencyKey = idempotencyKey;
        this.sender = sender;
        this.recipient = recipient;
        this.balance = balance;
        this.currency = currency;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID idempotencyKey;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Account recipient;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        return idempotencyKey != null && idempotencyKey.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
