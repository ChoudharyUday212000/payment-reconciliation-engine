package io.uday.reconciliation;

import java.util.List;

public record ReconciliationResult(
        String externalReference,
        ReconciliationStatus status,
        List<PaymentRecord> gatewayTransactions,
        List<PaymentRecord> ledgerEntries,
        Long amountDifferenceMinorUnits) {

    public ReconciliationResult {
        if (externalReference == null || externalReference.isBlank()) {
            throw new IllegalArgumentException("externalReference must not be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        gatewayTransactions = List.copyOf(gatewayTransactions);
        ledgerEntries = List.copyOf(ledgerEntries);
    }
}
