package com.example.sepa.paymentservice.repository;

import com.example.sepa.paymentservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    // Custom query method to find by the idempotency key string
    Optional<IdempotencyKey> findByKey(String key);
}