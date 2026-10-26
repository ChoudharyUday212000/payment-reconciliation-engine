package io.uday.reconciliation;

/**
 * A transaction observed in either the payment gateway or internal ledger.
 * Amounts use currency minor units (for example, paise or cents).
 */
public record PaymentRecord(
        String transactionId,
        String externalReference,
        long amountMinorUnits,
        String currency) {

    public PaymentRecord {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
        if (externalReference == null || externalReference.isBlank()) {
            throw new IllegalArgumentException("externalReference must not be blank");
        }
        if (amountMinorUnits < 0) {
            throw new IllegalArgumentException("amountMinorUnits must not be negative");
        }
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("currency must be a three-letter uppercase code");
        }
    }
}
