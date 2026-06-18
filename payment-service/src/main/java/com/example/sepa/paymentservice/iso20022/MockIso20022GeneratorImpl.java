package com.example.sepa.paymentservice.iso20022;

import com.example.sepa.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class MockIso20022GeneratorImpl implements Iso20022Generator {

    @Override
    public String generatePacs008(Payment payment) {
        // This is a simplified mock. In a real scenario, this would generate a full ISO 20022 XML.
        // For the showcase, we just return a string representation for demonstration.
        return String.format(
                "<Document><FIToFICstmrCdtTrf><GrpHdr><MsgId>%s</MsgId><CreDtTm>%s</CreDtTm></GrpHdr>" +
                        "<CdtTrfTxInf><PmtId><EndToEndId>%s</EndToEndId></PmtId><Amt><InstdAmt Ccy=\"%s\">%s</InstdAmt></Amt>" +
                        "<Dbtr><Nm>%s</Nm></Dbtr><Cdtr><Nm>%s</Nm></Cdtr><SvcLvl><Cd>%s</Cd></SvcLvl></FIToFICstmrCdtTrf></Document>",
                payment.getCorrelationId(), // Using correlationId as MsgId for simplicity
                payment.getCreatedAt().toString(),
                payment.getId(), // Using payment ID as EndToEndId
                payment.getCurrency(),
                payment.getAmount().toPlainString(),
                payment.getDebtorIban(), // Using IBAN as name for simplicity
                payment.getCreditorIban(), // Using IBAN as name for simplicity
                payment.getServiceLevel().name()
        );
    }
}