package io.uday.reconciliation;

public enum ReconciliationStatus {
    MATCHED,
    MISSING_IN_GATEWAY,
    MISSING_IN_LEDGER,
    AMOUNT_MISMATCH,
    CURRENCY_MISMATCH,
    DUPLICATE_GATEWAY,
    DUPLICATE_LEDGER,
    DUPLICATE_IN_BOTH
}
