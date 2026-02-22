package com.example.global_payment_service.transfer.idempotency;

import com.example.global_payment_service.transfer.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, UUID> {

    Optional<Idempotency> findByIdempotencyKey(UUID idempotencyKey);

    @Modifying(clearAutomatically = true)
    @Query("update Idempotency i set i.status = :status where i.idempotencyKey = :idempotencyKey")
    void setStatusWhereIdempotencyKey(TransferStatus status, UUID idempotencyKey);

}
