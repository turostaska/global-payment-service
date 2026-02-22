package com.example.global_payment_service.transfer.idempotency;

import com.example.global_payment_service.transfer.TransferStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Idempotency {

    public Idempotency(UUID idempotencyKey, TransferStatus status) {
        this.idempotencyKey = idempotencyKey;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID idempotencyKey;

    @Column(nullable = false)
    private TransferStatus status;
}
