package com.airtribe.meditrack.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Simple immutable class for billing summary.
 * Demonstrates immutability - once created, values cannot be changed!
 * 
 * Key features:
 * - Class is final (cannot be extended)
 * - All fields are final (cannot be modified after creation)
 * - No setter methods (only getters)
 * - Thread-safe by design
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public final class BillSummary implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // All fields are final - cannot be changed after object creation
    private final String summaryId;
    private final String patientId;
    private final String patientName;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final int totalBills;
    private final double totalAmount;
    private final double paidAmount;
    private final double pendingAmount;
    private final LocalDateTime createdAt;

    /**
     * Simple constructor for immutable BillSummary.
     * Once created, values cannot be changed!
     */
    public BillSummary(String summaryId, String patientId, String patientName, 
                      LocalDate periodStart, LocalDate periodEnd, 
                      int totalBills, double totalAmount, double paidAmount) {
        // Set all final fields - no way to change them later
        this.summaryId = summaryId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalBills = totalBills;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.pendingAmount = totalAmount - paidAmount;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Factory method to create BillSummary from a list of bills.
     * Simple version that calculates basic totals.
     */
    public static BillSummary fromBills(String summaryId, String patientId, String patientName,
                                       List<Bill> bills, LocalDate periodStart, LocalDate periodEnd) {
        if (bills == null || bills.isEmpty()) {
            return new BillSummary(summaryId, patientId, patientName, periodStart, periodEnd, 0, 0.0, 0.0);
        }
        
        int totalBills = bills.size();
        double totalAmount = 0.0;
        double paidAmount = 0.0;
        
        // Simple calculation
        for (Bill bill : bills) {
            totalAmount += bill.calculateTotalAmount();
            if (bill.isPaid()) {
                paidAmount += bill.calculateTotalAmount();
            }
        }
        
        return new BillSummary(summaryId, patientId, patientName, periodStart, periodEnd, 
                             totalBills, totalAmount, paidAmount);
    }
    
    // Only getters - no setters for immutability!
    
    public String getSummaryId() {
        return summaryId;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public LocalDate getPeriodStart() {
        return periodStart;
    }
    
    public LocalDate getPeriodEnd() {
        return periodEnd;
    }
    
    public int getTotalBills() {
        return totalBills;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public double getPaidAmount() {
        return paidAmount;
    }
    
    public double getPendingAmount() {
        return pendingAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Simple calculated properties
    
    /**
     * Gets payment completion rate.
     * @return percentage paid (0.0 to 1.0)
     */
    public double getPaymentCompletionRate() {
        return totalAmount > 0 ? paidAmount / totalAmount : 0.0;
    }
    
    /**
     * Gets average bill amount.
     * @return average amount per bill
     */
    public double getAverageBillAmount() {
        return totalBills > 0 ? totalAmount / totalBills : 0.0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BillSummary that = (BillSummary) obj;
        return summaryId != null ? summaryId.equals(that.summaryId) : that.summaryId == null;
    }
    
    @Override
    public int hashCode() {
        return summaryId != null ? summaryId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("BillSummary{id='%s', patient='%s', period='%s to %s', " +
                           "bills=%d, total=₹%.2f, paid=₹%.2f}", 
                summaryId, patientName, periodStart, periodEnd, 
                totalBills, totalAmount, paidAmount);
    }
    
    /**
     * Creates a simple summary report.
     * @return formatted summary string
     */
    public String getSimpleReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== BILLING SUMMARY ===\n");
        report.append("Summary ID: ").append(summaryId).append("\n");
        report.append("Patient: ").append(patientName).append("\n");
        report.append("Period: ").append(periodStart).append(" to ").append(periodEnd).append("\n");
        report.append("Total Bills: ").append(totalBills).append("\n");
        report.append("Total Amount: ₹").append(String.format("%.2f", totalAmount)).append("\n");
        report.append("Paid Amount: ₹").append(String.format("%.2f", paidAmount)).append("\n");
        report.append("Pending Amount: ₹").append(String.format("%.2f", pendingAmount)).append("\n");
        report.append("Payment Rate: ").append(String.format("%.1f%%", getPaymentCompletionRate() * 100)).append("\n");
        report.append("Average Bill: ₹").append(String.format("%.2f", getAverageBillAmount())).append("\n");
        report.append("======================\n");
        return report.toString();
    }
}