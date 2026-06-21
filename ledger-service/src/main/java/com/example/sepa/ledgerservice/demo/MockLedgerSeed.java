package com.example.sepa.ledgerservice.demo;

import com.example.sepa.ledgerservice.entity.Account;
import com.example.sepa.ledgerservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Seeds demo accounts on startup when profile 'showcase' is active.
 */
@Component
@Profile("showcase")
@RequiredArgsConstructor
@Slf4j
public class MockLedgerSeed implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        log.info("Showcase profile active - seeding ledger accounts...");

        createAccountIfMissing("DE12500105170648489890", new BigDecimal("1000.00"));
        createAccountIfMissing("NL39RABO0300065264", new BigDecimal("500.00"));
        createAccountIfMissing("FR7630006000011234567890189", new BigDecimal("750.00"));
        createAccountIfMissing("DE89370400440532013000", new BigDecimal("300.00"));
        createAccountIfMissing("ES9121000418450200051332", new BigDecimal("200.00"));
        createAccountIfMissing("IT60X0542811101000000123456", new BigDecimal("100.00"));

        log.info("Ledger accounts seeded.");
    }

    private void createAccountIfMissing(String iban, BigDecimal balance) {
        accountRepository.findById(iban).ifPresentOrElse(acc -> {
            log.info("Account {} already exists, skipping seed", iban);
        }, () -> {
            Account a = Account.builder()
                    .iban(iban)
                    .balance(balance)
                    .createdAt(Instant.now())
                    .lastUpdatedAt(Instant.now())
                    .build();
            accountRepository.save(a);
            log.info("Created account {} with balance {}", iban, balance);
        });
    }
}