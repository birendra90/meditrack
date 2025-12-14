package com.airtribe.meditrack.interfaces;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Interface for entities that can be billed or paid.
 * This demonstrates interface design with payment-related operations.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public interface Payable {
    
    /**
     * Get the base amount before any taxes or discounts.
     * 
     * @return the base amount
     */
    double getBaseAmount();
    
    /**
     * Calculate the total amount including taxes and discounts.
     * 
     * @return the total payable amount
     */
    double calculateTotalAmount();
    
    /**
     * Get the tax amount for this payable item.
     * 
     * @return the tax amount
     */
    double getTaxAmount();
    
    /**
     * Get the discount amount applied to this payable item.
     * 
     * @return the discount amount
     */
    double getDiscountAmount();
    
    /**
     * Check if this item is paid.
     * 
     * @return true if paid, false otherwise
     */
    boolean isPaid();
    
    /**
     * Get the payment date and time.
     * 
     * @return the payment date time, or null if not paid
     */
    LocalDateTime getPaymentDateTime();
    
    /**
     * Get the payment method used.
     * 
     * @return the payment method, or null if not paid
     */
    String getPaymentMethod();
    
    /**
     * Default implementation to calculate tax based on base amount and tax rate.
     * 
     * @param taxRate the tax rate (as decimal, e.g., 0.18 for 18%)
     * @return the calculated tax amount
     */
    default double calculateTax(double taxRate) {
        return getBaseAmount() * taxRate;
    }
    
    /**
     * Default implementation to apply percentage discount.
     * 
     * @param discountPercentage the discount percentage (as decimal, e.g., 0.10 for 10%)
     * @return the discount amount
     */
    default double calculatePercentageDiscount(double discountPercentage) {
        return getBaseAmount() * discountPercentage;
    }
    
    /**
     * Default implementation to apply flat discount.
     * 
     * @param discountAmount the flat discount amount
     * @return the effective discount amount (cannot exceed base amount)
     */
    default double calculateFlatDiscount(double discountAmount) {
        return Math.min(discountAmount, getBaseAmount());
    }
    
    /**
     * Default implementation to check if item qualifies for senior citizen discount.
     * 
     * @param age the age of the person
     * @param seniorAgeThreshold the age threshold for senior citizen (default 65)
     * @return true if qualifies for senior discount
     */
    default boolean qualifiesForSeniorDiscount(int age, int seniorAgeThreshold) {
        return age >= seniorAgeThreshold;
    }
    
    /**
     * Default implementation with standard senior age threshold (65).
     * 
     * @param age the age of the person
     * @return true if qualifies for senior discount
     */
    default boolean qualifiesForSeniorDiscount(int age) {
        return qualifiesForSeniorDiscount(age, 65);
    }
    
    /**
     * Default implementation to calculate total with standard tax and discount logic.
     * 
     * @param taxRate the tax rate to apply
     * @param discountRate the discount rate to apply
     * @return the total amount after tax and discount
     */
    default double calculateTotalWithTaxAndDiscount(double taxRate, double discountRate) {
        double baseAmount = getBaseAmount();
        double discountAmount = baseAmount * discountRate;
        double discountedAmount = baseAmount - discountAmount;
        double taxAmount = discountedAmount * taxRate;
        return discountedAmount + taxAmount;
    }
    
    /**
     * Get a breakdown of the payment calculation.
     * 
     * @return map containing payment breakdown details
     */
    default Map<String, Double> getPaymentBreakdown() {
        Map<String, Double> breakdown = new java.util.LinkedHashMap<>();
        breakdown.put("Base Amount", getBaseAmount());
        breakdown.put("Discount Amount", getDiscountAmount());
        breakdown.put("Amount After Discount", getBaseAmount() - getDiscountAmount());
        breakdown.put("Tax Amount", getTaxAmount());
        breakdown.put("Total Amount", calculateTotalAmount());
        return breakdown;
    }
    
    /**
     * Get a formatted payment summary string.
     * 
     * @return formatted payment summary
     */
    default String getPaymentSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Payment Summary:\n");
        summary.append("-".repeat(30)).append("\n");
        
        Map<String, Double> breakdown = getPaymentBreakdown();
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            summary.append(String.format("%-20s: ₹%.2f\n", entry.getKey(), entry.getValue()));
        }
        
        summary.append("-".repeat(30)).append("\n");
        summary.append(String.format("Status: %s\n", isPaid() ? "PAID" : "PENDING"));
        
        if (isPaid()) {
            summary.append(String.format("Paid on: %s\n", getPaymentDateTime()));
            if (getPaymentMethod() != null) {
                summary.append(String.format("Payment Method: %s\n", getPaymentMethod()));
            }
        }
        
        return summary.toString();
    }
    
    /**
     * Default implementation to check if payment is overdue.
     * 
     * @param dueDate the due date for payment
     * @return true if payment is overdue
     */
    default boolean isOverdue(LocalDateTime dueDate) {
        return !isPaid() && LocalDateTime.now().isAfter(dueDate);
    }
    
    /**
     * Default implementation to calculate late fees.
     * 
     * @param dueDate the due date for payment
     * @param dailyLateFee the daily late fee amount
     * @return the calculated late fee
     */
    default double calculateLateFee(LocalDateTime dueDate, double dailyLateFee) {
        if (!isOverdue(dueDate)) {
            return 0.0;
        }
        
        long daysOverdue = java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
        return daysOverdue * dailyLateFee;
    }
    
    /**
     * Default implementation to get payment status as string.
     * 
     * @return payment status string
     */
    default String getPaymentStatus() {
        if (isPaid()) {
            return "PAID";
        } else {
            return "PENDING";
        }
    }
    
    /**
     * Default implementation to validate payment amount.
     * 
     * @param paymentAmount the amount being paid
     * @return true if payment amount is valid
     */
    default boolean isValidPaymentAmount(double paymentAmount) {
        double totalAmount = calculateTotalAmount();
        return paymentAmount > 0 && paymentAmount <= totalAmount;
    }
    
    /**
     * Default implementation to calculate change if overpaid.
     * 
     * @param paymentAmount the amount paid
     * @return the change amount (0 if exact or underpaid)
     */
    default double calculateChange(double paymentAmount) {
        double totalAmount = calculateTotalAmount();
        return paymentAmount > totalAmount ? paymentAmount - totalAmount : 0.0;
    }
    
    /**
     * Default implementation to check if partial payments are allowed.
     * Override this method to customize partial payment behavior.
     * 
     * @return true if partial payments are allowed
     */
    default boolean allowsPartialPayments() {
        return false;
    }
    
    /**
     * Get the minimum payment amount (for partial payments).
     * 
     * @return minimum payment amount
     */
    default double getMinimumPaymentAmount() {
        return allowsPartialPayments() ? calculateTotalAmount() * 0.1 : calculateTotalAmount();
    }
}