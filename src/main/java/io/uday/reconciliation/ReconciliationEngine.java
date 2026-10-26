package io.uday.reconciliation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Compares gateway and ledger snapshots by shared external reference.
 * Output references are sorted and input collections are not mutated.
 */
public final class ReconciliationEngine {

    public List<ReconciliationResult> reconcile(
            List<PaymentRecord> gatewayTransactions,
            List<PaymentRecord> ledgerEntries) {

        Objects.requireNonNull(gatewayTransactions, "gatewayTransactions must not be null");
        Objects.requireNonNull(ledgerEntries, "ledgerEntries must not be null");

        Map<String, List<PaymentRecord>> gatewayByReference = groupByReference(gatewayTransactions);
        Map<String, List<PaymentRecord>> ledgerByReference = groupByReference(ledgerEntries);

        Set<String> references = new TreeSet<>();
        references.addAll(gatewayByReference.keySet());
        references.addAll(ledgerByReference.keySet());

        List<ReconciliationResult> results = new ArrayList<>(references.size());
        for (String reference : references) {
            List<PaymentRecord> gateway = gatewayByReference.getOrDefault(reference, List.of());
            List<PaymentRecord> ledger = ledgerByReference.getOrDefault(reference, List.of());
            results.add(compare(reference, gateway, ledger));
        }
        return List.copyOf(results);
    }

    private ReconciliationResult compare(
            String reference,
            List<PaymentRecord> gateway,
            List<PaymentRecord> ledger) {

        ReconciliationStatus status;
        Long difference = null;

        if (gateway.size() > 1 && ledger.size() > 1) {
            status = ReconciliationStatus.DUPLICATE_IN_BOTH;
        } else if (gateway.size() > 1) {
            status = ReconciliationStatus.DUPLICATE_GATEWAY;
        } else if (ledger.size() > 1) {
            status = ReconciliationStatus.DUPLICATE_LEDGER;
        } else if (gateway.isEmpty()) {
            status = ReconciliationStatus.MISSING_IN_GATEWAY;
        } else if (ledger.isEmpty()) {
            status = ReconciliationStatus.MISSING_IN_LEDGER;
        } else {
            PaymentRecord gatewayRecord = gateway.get(0);
            PaymentRecord ledgerRecord = ledger.get(0);
            if (!gatewayRecord.currency().equals(ledgerRecord.currency())) {
                status = ReconciliationStatus.CURRENCY_MISMATCH;
            } else if (gatewayRecord.amountMinorUnits() != ledgerRecord.amountMinorUnits()) {
                status = ReconciliationStatus.AMOUNT_MISMATCH;
                difference = Math.subtractExact(
                        ledgerRecord.amountMinorUnits(), gatewayRecord.amountMinorUnits());
            } else {
                status = ReconciliationStatus.MATCHED;
                difference = 0L;
            }
        }

        return new ReconciliationResult(reference, status, gateway, ledger, difference);
    }

    private Map<String, List<PaymentRecord>> groupByReference(Collection<PaymentRecord> records) {
        return records.stream().collect(Collectors.groupingBy(
                PaymentRecord::externalReference, TreeMap::new, Collectors.toList()));
    }
}
