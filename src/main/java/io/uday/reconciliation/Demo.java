package io.uday.reconciliation;

import java.util.List;

public final class Demo {

    private Demo() {
    }

    public static void main(String[] args) {
        List<PaymentRecord> gateway = List.of(
                new PaymentRecord("gw-101", "order-101", 12_500, "INR"),
                new PaymentRecord("gw-102", "order-102", 7_000, "INR"),
                new PaymentRecord("gw-103", "order-103", 4_500, "INR"),
                new PaymentRecord("gw-104-a", "order-104", 2_000, "INR"),
                new PaymentRecord("gw-104-b", "order-104", 2_000, "INR"));

        List<PaymentRecord> ledger = List.of(
                new PaymentRecord("ledger-101", "order-101", 12_500, "INR"),
                new PaymentRecord("ledger-102", "order-102", 6_800, "INR"),
                new PaymentRecord("ledger-105", "order-105", 3_000, "INR"));

        new ReconciliationEngine().reconcile(gateway, ledger).forEach(result ->
                System.out.printf("%-12s %-20s gateway=%d ledger=%d delta=%s%n",
                        result.status(), result.externalReference(),
                        result.gatewayTransactions().size(), result.ledgerEntries().size(),
                        result.amountDifferenceMinorUnits()));
    }
}
