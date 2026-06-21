package com.example.sepa.ledgerservice.service;

import com.example.sepa.common.event.PaymentStatusEvent;
import com.example.sepa.ledgerservice.entity.Account;
import com.example.sepa.ledgerservice.entity.LedgerEntry;
import com.example.sepa.ledgerservice.repository.AccountRepository;
import com.example.sepa.ledgerservice.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerConsumer {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @KafkaListener(topics = "${app.kafka.topics.payment-status}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onPaymentStatus(PaymentStatusEvent event) {
        log.info("LedgerConsumer received PaymentStatusEvent: paymentId={} status={}", event.getPaymentId(), event.getStatus());

        // Only act for accepted payments (business decision)
        if (!"ACCEPTED".equalsIgnoreCase(event.getStatus())) {
            log.info("Payment {} status is {}, skipping ledger posting", event.getPaymentId(), event.getStatus());
            return;
        }

        if (event.getAmount() == null || event.getDebtorIban() == null || event.getCreditorIban() == null || event.getCurrency() == null) {
            log.warn("PaymentStatusEvent for {} lacks required ledger fields (amount/debtor/creditor/currency). Event: {}", event.getPaymentId(), event);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(event.getAmount());
        } catch (NumberFormatException e) {
            log.error("Invalid amount on PaymentStatusEvent for {}: {}", event.getPaymentId(), event.getAmount(), e);
            return;
        }

        String paymentId = event.getPaymentId();

        // Idempotency check: has this payment already been processed?
        if (ledgerEntryRepository.existsByPaymentId(paymentId)) {
            log.info("Ledger entry for payment {} already exists, skipping (idempotent)", paymentId);
            return;
        }

        // Create ledger entry record (but be resilient to race/unique-constraint)
        LedgerEntry newEntry = LedgerEntry.builder()
                .paymentId(paymentId)
                .amount(amount)
                .currency(event.getCurrency())
                .debtorIban(event.getDebtorIban())
                .creditorIban(event.getCreditorIban())
                .createdAt(Instant.now())
                .build();

        try {
            ledgerEntryRepository.save(newEntry);
        } catch (DataIntegrityViolationException dive) {
            // Another instance probably saved the entry concurrently; treat as already processed
            log.warn("Ledger entry for payment {} already inserted by concurrent process, skipping ledger posting", paymentId);
            return;
        }

        // Apply account updates (naive approach for demo)
        Account debtor = accountRepository.findById(event.getDebtorIban()).orElseGet(() -> {
            log.info("Creating missing debtor account {}", event.getDebtorIban());
            Account a = Account.builder()
                    .iban(event.getDebtorIban())
                    .balance(BigDecimal.ZERO)
                    .createdAt(Instant.now())
                    .lastUpdatedAt(Instant.now())
                    .build();
            return accountRepository.save(a);
        });

        Account creditor = accountRepository.findById(event.getCreditorIban()).orElseGet(() -> {
            log.info("Creating missing creditor account {}", event.getCreditorIban());
            Account a = Account.builder()
                    .iban(event.getCreditorIban())
                    .balance(BigDecimal.ZERO)
                    .createdAt(Instant.now())
                    .lastUpdatedAt(Instant.now())
                    .build();
            return accountRepository.save(a);
        });

        // Update balances
        debtor.setBalance(debtor.getBalance().subtract(amount));
        debtor.setLastUpdatedAt(Instant.now());
        accountRepository.save(debtor);

        creditor.setBalance(creditor.getBalance().add(amount));
        creditor.setLastUpdatedAt(Instant.now());
        accountRepository.save(creditor);

        log.info("Ledger updated for payment {}: debited {} from {}, credited {} to {}", paymentId,
                amount, event.getDebtorIban(), amount, event.getCreditorIban());
    }
}