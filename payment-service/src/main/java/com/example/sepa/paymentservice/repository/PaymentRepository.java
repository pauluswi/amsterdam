package com.example.sepa.paymentservice.repository;

import com.example.sepa.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    // Custom query methods if needed
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}