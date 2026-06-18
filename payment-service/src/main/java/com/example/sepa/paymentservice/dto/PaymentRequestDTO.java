package com.example.sepa.paymentservice.dto;

import com.example.sepa.common.ServiceLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Service level cannot be null")
    private ServiceLevel serviceLevel;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency cannot be blank")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotBlank(message = "Debtor IBAN cannot be blank")
    @Pattern(regexp = "[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}", message = "Invalid Debtor IBAN format")
    private String debtorIban;

    @NotBlank(message = "Creditor IBAN cannot be blank")
    @Pattern(regexp = "[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}", message = "Invalid Creditor IBAN format")
    private String creditorIban;

}
