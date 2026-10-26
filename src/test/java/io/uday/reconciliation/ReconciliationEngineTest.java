package io.uday.reconciliation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void classifiesMatchedTransactions() {
        var gateway = new PaymentRecord("gw-1", "ref-1", 1500, "INR");
        var ledger = new PaymentRecord("ledger-1", "ref-1", 1500, "INR");
        var result = engine.reconcile(List.of(gateway), List.of(ledger)).get(0);

        assertEquals(ReconciliationStatus.MATCHED, result.status());
        assertEquals(0L, result.amountDifferenceMinorUnits());
    }

    @Test
    void reportsSignedAmountDifferenceAsLedgerMinusGateway() {
        var gateway = new PaymentRecord("gw-1", "ref-1", 1800, "INR");
        var ledger = new PaymentRecord("ledger-1", "ref-1", 1500, "INR");
        var result = engine.reconcile(List.of(gateway), List.of(ledger)).get(0);

        assertEquals(ReconciliationStatus.AMOUNT_MISMATCH, result.status());
        assertEquals(-300L, result.amountDifferenceMinorUnits());
    }

    @Test
    void detectsCurrencyMismatchBeforeComparingAmounts() {
        var gateway = new PaymentRecord("gw-1", "ref-1", 1500, "INR");
        var ledger = new PaymentRecord("ledger-1", "ref-1", 1500, "USD");
        var result = engine.reconcile(List.of(gateway), List.of(ledger)).get(0);

        assertEquals(ReconciliationStatus.CURRENCY_MISMATCH, result.status());
        assertNull(result.amountDifferenceMinorUnits());
    }

    @Test
    void detectsMissingRecordsOnEitherSide() {
        var gatewayOnly = new PaymentRecord("gw-1", "gateway-only", 500, "INR");
        var ledgerOnly = new PaymentRecord("ledger-1", "ledger-only", 600, "INR");
        var results = engine.reconcile(List.of(gatewayOnly), List.of(ledgerOnly));
        var byReference = results.stream().collect(Collectors.toMap(
                ReconciliationResult::externalReference, Function.identity()));

        assertEquals(ReconciliationStatus.MISSING_IN_LEDGER, byReference.get("gateway-only").status());
        assertEquals(ReconciliationStatus.MISSING_IN_GATEWAY, byReference.get("ledger-only").status());
    }

    @Test
    void detectsDuplicatesAndRetainsEveryRecordForInvestigation() {
        var gateway1 = new PaymentRecord("gw-1", "ref-1", 500, "INR");
        var gateway2 = new PaymentRecord("gw-2", "ref-1", 500, "INR");
        var ledger1 = new PaymentRecord("ledger-1", "ref-2", 500, "INR");
        var ledger2 = new PaymentRecord("ledger-2", "ref-2", 500, "INR");
        var results = engine.reconcile(List.of(gateway1, gateway2), List.of(ledger1, ledger2));
        var byReference = results.stream().collect(Collectors.toMap(
                ReconciliationResult::externalReference, Function.identity()));

        assertEquals(ReconciliationStatus.DUPLICATE_GATEWAY, byReference.get("ref-1").status());
        assertEquals(2, byReference.get("ref-1").gatewayTransactions().size());
        assertEquals(ReconciliationStatus.DUPLICATE_LEDGER, byReference.get("ref-2").status());
    }

    @Test
    void producesStableSortedOutput() {
        var a = new PaymentRecord("gw-a", "a-ref", 1, "INR");
        var z = new PaymentRecord("gw-z", "z-ref", 1, "INR");
        var results = engine.reconcile(List.of(z, a), List.of());

        assertEquals(List.of("a-ref", "z-ref"),
                results.stream().map(ReconciliationResult::externalReference).toList());
    }

    @Test
    void validatesTransactionData() {
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRecord("gw-1", "ref-1", -1, "INR"));
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRecord("gw-1", "ref-1", 100, "inr"));
    }
}
