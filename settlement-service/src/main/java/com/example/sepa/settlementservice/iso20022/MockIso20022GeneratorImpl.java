package com.example.sepa.settlementservice.iso20022;

import com.example.sepa.common.event.PaymentInitiatedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MockIso20022GeneratorImpl implements Iso20022Generator {

    @Override
    public String generatePacs002(PaymentInitiatedEvent paymentEvent, String status, String reason) {
        // This is a simplified mock. In a real scenario, this would generate a full ISO 20022 XML.
        // For the showcase, we just return a string representation for demonstration.
        return String.format(
                "<Document><FIToFIPmtStsRpt><GrpHdr><MsgId>%s</MsgId><CreDtTm>%s</CreDtTm></GrpHdr>" +
                        "<OrgnlGrpInfAndSts><OrgnlMsgId>%s</OrgnlMsgId><OrgnlMsgNmId>pacs.008.001.02</OrgnlMsgNmId><GrpSts>%s</GrpSts></OrgnlGrpInfAndSts>" +
                        "<TxInfAndSts><OrgnlEndToEndId>%s</OrgnlEndToEndId><TxSts>%s</TxSts><StsRsn><Prtry>%s</Prtry></StsRsn></TxInfAndSts>" +
                        "</FIToFIPmtStsRpt></Document>",
                "PACS002-" + paymentEvent.getPaymentId(), // Mock MsgId for pacs.002
                Instant.now().toString(),
                paymentEvent.getCorrelationId(), // Original MsgId from pacs.008
                status, // Group Status
                paymentEvent.getPaymentId(), // Original EndToEndId
                status, // Transaction Status
                reason != null ? reason : "N/A"
        );
    }
}