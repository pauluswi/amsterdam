package com.example.sepa.paymentservice.entity;

import com.example.sepa.common.ServiceLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    private String id; // Using paymentId as the primary key

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceLevel serviceLevel;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String debtorIban;

    @Column(nullable = false)
    private String creditorIban;

    @Column(nullable = false)
    private String status; // e.g., INITIATED, ACCEPTED, REJECTED, RETURNED

    @Column(nullable = false)
    private Instant createdAt;

    private Instant lastUpdatedAt;

    @Column(nullable = false, unique = true)
    private String correlationId; // ADR-008

    @Column(nullable = false, unique = true)
    private String idempotencyKey; // ADR-006
}