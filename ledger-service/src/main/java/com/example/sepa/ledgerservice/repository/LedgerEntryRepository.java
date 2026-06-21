package com.example.sepa.ledgerservice.repository;

import com.example.sepa.ledgerservice.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {
    boolean existsByPaymentId(String paymentId);
    Optional<LedgerEntry> findByPaymentId(String paymentId);
}