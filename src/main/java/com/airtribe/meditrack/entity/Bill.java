package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.interfaces.Payable;
import com.airtribe.meditrack.interfaces.Searchable;
import com.airtribe.meditrack.constants.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Bill entity representing medical bills in the system.
 * This demonstrates the Payable interface implementation and billing logic.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class Bill extends MedicalEntity implements Payable, Searchable<Bill> {
    
    private static final long serialVersionUID = 1L;
    
    // Bill-specific fields
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private double baseAmount;
    private double discountAmount;
    private double taxAmount;
    private double totalAmount;
    private boolean isPaid;
    private LocalDateTime paymentDateTime;
    private String paymentMethod;
    private String paymentReference;
    private String billType; // CONSULTATION, SURGERY, EMERGENCY, MEDICATION
    private List<BillItem> billItems;
    private String insuranceClaimId;
    private double insuranceCoverage;
    private String billNotes;
    private LocalDateTime dueDate;
    private String generatedBy; // User/system who generated the bill
    
    // References to related entities
    private transient Appointment appointment;
    private transient Patient patient;
    private transient Doctor doctor;
    
    // Static counter for tracking total bills
    private static int totalBills = 0;
    
    // Static initialization block
    static {
        System.out.println("[STATIC BLOCK] Bill class initialized");
    }
    
    /**
     * Inner class representing individual bill items.
     * Demonstrates nested class usage.
     */
    public static class BillItem {
        private String description;
        private int quantity;
        private double unitPrice;
        private double totalPrice;
        
        public BillItem(String description, int quantity, double unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
        }
        
        // Getters
        public String getDescription() { return description; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalPrice() { return totalPrice; }
        
        @Override
        public String toString() {
            return String.format("%s (Qty: %d × ₹%.2f = ₹%.2f)", 
                    description, quantity, unitPrice, totalPrice);
        }
    }
    
    /**
     * Default constructor.
     */
    public Bill() {
        super();
        initializeBill();
    }
    
    /**
     * Constructor with basic information.
     */
    public Bill(String id, String appointmentId, String patientId, String doctorId, 
               double baseAmount, String billType) {
        super(id);
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.baseAmount = baseAmount;
        this.billType = billType;
        initializeBill();
        calculateAmounts();
    }
    
    /**
     * Constructor with complete information.
     */
    public Bill(String id, String appointmentId, String patientId, String doctorId,
               double baseAmount, String billType, List<BillItem> billItems,
               String generatedBy) {
        super(id);
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.baseAmount = baseAmount;
        this.billType = billType;
        this.billItems = new ArrayList<>(billItems);
        this.generatedBy = generatedBy;
        initializeBill();
        calculateAmounts();
    }
    
    /**
     * Initializes bill-specific defaults.
     */
    private void initializeBill() {
        this.billItems = new ArrayList<>();
        this.isPaid = false;
        this.discountAmount = 0.0;
        this.taxAmount = 0.0;
        this.insuranceCoverage = 0.0;
        this.dueDate = LocalDateTime.now().plusDays(30); // 30 days payment term
        
        synchronized (Bill.class) {
            totalBills++;
        }
    }
    
    /**
     * Calculates tax, discount, and total amounts based on business rules.
     */
    private void calculateAmounts() {
        // Calculate base amount from consultation fee and bill items
        double itemsTotal = billItems.stream().mapToDouble(BillItem::getTotalPrice).sum();
        double effectiveBaseAmount = baseAmount + itemsTotal;
        
        // Apply discounts
        calculateDiscount(effectiveBaseAmount);
        
        // Calculate amount after discount
        double discountedAmount = effectiveBaseAmount - discountAmount;
        
        // Calculate tax on discounted amount
        this.taxAmount = Constants.calculateTax(discountedAmount);
        
        // Calculate total amount
        this.totalAmount = discountedAmount + taxAmount - insuranceCoverage;
        
        // Ensure total amount is not negative
        this.totalAmount = Math.max(this.totalAmount, 0.0);
    }
    
    /**
     * Calculates applicable discounts based on patient and amount.
     */
    private void calculateDiscount(double amount) {
        this.discountAmount = 0.0;
        
        // Senior citizen discount
        if (patient != null && patient.isSeniorCitizen()) {
            this.discountAmount += amount * Constants.SENIOR_CITIZEN_DISCOUNT;
        }
        
        // Insurance discount
        if (patient != null && patient.hasInsurance()) {
            this.discountAmount += amount * Constants.INSURANCE_DISCOUNT;
        }
        
        // Amount-based discount
        if (Constants.qualifiesForDiscount(amount)) {
            this.discountAmount += amount * 0.05; // 5% discount for high amounts
        }
    }
    
    // Getters and Setters
    
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
        updateTimestamp();
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
        updateTimestamp();
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
        updateTimestamp();
    }
    
    public String getBillType() {
        return billType;
    }
    
    public void setBillType(String billType) {
        this.billType = billType;
        updateTimestamp();
    }
    
    public List<BillItem> getBillItems() {
        return new ArrayList<>(billItems); // Return defensive copy
    }
    
    public void setBillItems(List<BillItem> billItems) {
        this.billItems = new ArrayList<>(billItems);
        calculateAmounts();
        updateTimestamp();
    }
    
    public void addBillItem(String description, int quantity, double unitPrice) {
        BillItem item = new BillItem(description, quantity, unitPrice);
        this.billItems.add(item);
        calculateAmounts();
        updateTimestamp();
    }
    
    public void addBillItem(BillItem item) {
        this.billItems.add(item);
        calculateAmounts();
        updateTimestamp();
    }
    
    public String getInsuranceClaimId() {
        return insuranceClaimId;
    }
    
    public void setInsuranceClaimId(String insuranceClaimId) {
        this.insuranceClaimId = insuranceClaimId;
        updateTimestamp();
    }
    
    public double getInsuranceCoverage() {
        return insuranceCoverage;
    }
    
    public void setInsuranceCoverage(double insuranceCoverage) {
        this.insuranceCoverage = insuranceCoverage;
        calculateAmounts();
        updateTimestamp();
    }
    
    public String getBillNotes() {
        return billNotes;
    }
    
    public void setBillNotes(String billNotes) {
        this.billNotes = billNotes;
        updateTimestamp();
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
        updateTimestamp();
    }
    
    public String getGeneratedBy() {
        return generatedBy;
    }
    
    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
        updateTimestamp();
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
        if (appointment != null) {
            this.appointmentId = appointment.getId();
            this.patientId = appointment.getPatientId();
            this.doctorId = appointment.getDoctorId();
            this.baseAmount = appointment.getConsultationFee();
        }
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getId();
            calculateAmounts(); // Recalculate with patient-specific discounts
        }
    }
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getId();
        }
    }
    
    // Static method to get total bills count
    public static int getTotalBills() {
        return totalBills;
    }
    
    // Implementation of Payable interface
    
    @Override
    public double getBaseAmount() {
        return baseAmount + billItems.stream().mapToDouble(BillItem::getTotalPrice).sum();
    }
    
    @Override
    public double calculateTotalAmount() {
        return totalAmount;
    }
    
    @Override
    public double getTaxAmount() {
        return taxAmount;
    }
    
    @Override
    public double getDiscountAmount() {
        return discountAmount;
    }
    
    @Override
    public boolean isPaid() {
        return isPaid;
    }
    
    @Override
    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }
    
    @Override
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    // Payment processing methods
    
    /**
     * Processes payment for this bill.
     * 
     * @param paymentAmount the amount being paid
     * @param paymentMethod the payment method used
     * @param paymentReference the payment reference/transaction ID
     * @return true if payment was successful
     */
    public boolean processPayment(double paymentAmount, String paymentMethod, String paymentReference) {
        if (isPaid) {
            throw new IllegalStateException("Bill is already paid");
        }
        
        if (!isValidPaymentAmount(paymentAmount)) {
            throw new IllegalArgumentException("Invalid payment amount: " + paymentAmount);
        }
        
        this.isPaid = true;
        this.paymentDateTime = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        
        updateTimestamp();
        return true;
    }
    
    /**
     * Processes partial payment (if allowed).
     * 
     * @param paymentAmount the partial payment amount
     * @param paymentMethod the payment method used
     * @param paymentReference the payment reference
     * @return remaining balance
     */
    public double processPartialPayment(double paymentAmount, String paymentMethod, String paymentReference) {
        if (!allowsPartialPayments()) {
            throw new UnsupportedOperationException("Partial payments not allowed for this bill");
        }
        
        if (paymentAmount < getMinimumPaymentAmount()) {
            throw new IllegalArgumentException("Payment amount below minimum: " + getMinimumPaymentAmount());
        }
        
        // For simplicity, we'll track partial payments in notes
        String partialPaymentNote = String.format("Partial payment: ₹%.2f via %s (Ref: %s) on %s", 
                paymentAmount, paymentMethod, paymentReference, LocalDateTime.now());
        
        this.billNotes = (this.billNotes != null ? this.billNotes + "\n" : "") + partialPaymentNote;
        this.totalAmount -= paymentAmount;
        
        if (this.totalAmount <= 0) {
            this.isPaid = true;
            this.paymentDateTime = LocalDateTime.now();
            this.paymentMethod = paymentMethod;
            this.paymentReference = paymentReference;
            this.totalAmount = 0;
        }
        
        updateTimestamp();
        return Math.max(this.totalAmount, 0);
    }
    
    @Override
    public boolean allowsPartialPayments() {
        // Allow partial payments for bills over ₹5000
        return calculateTotalAmount() > 5000.0;
    }
    
    // Implementation of Searchable interface
    
    @Override
    public String getSearchId() {
        return getId();
    }
    
    @Override
    public String getPrimarySearchTerm() {
        return getId() + " - " + billType;
    }
    
    @Override
    public List<String> getSearchableTerms() {
        List<String> terms = new ArrayList<>();
        
        if (getId() != null) terms.add(getId());
        if (appointmentId != null) terms.add(appointmentId);
        if (patientId != null) terms.add(patientId);
        if (doctorId != null) terms.add(doctorId);
        if (billType != null) terms.add(billType);
        if (paymentMethod != null) terms.add(paymentMethod);
        if (paymentReference != null) terms.add(paymentReference);
        if (insuranceClaimId != null) terms.add(insuranceClaimId);
        if (generatedBy != null) terms.add(generatedBy);
        
        // Add amount ranges for searching
        terms.add("₹" + String.format("%.0f", totalAmount));
        if (isPaid) {
            terms.add("PAID");
        } else {
            terms.add("UNPAID");
        }
        
        return terms;
    }
    
    @Override
    public boolean matches(String criteria, boolean caseSensitive) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return false;
        }
        
        // Exact ID match
        if (getId() != null && getId().equals(criteria)) {
            return true;
        }
        
        // Appointment, Patient, or Doctor ID match
        if ((appointmentId != null && appointmentId.equals(criteria)) ||
            (patientId != null && patientId.equals(criteria)) ||
            (doctorId != null && doctorId.equals(criteria))) {
            return true;
        }
        
        // Use the default implementation from interface
        return matchesAny(criteria, caseSensitive);
    }
    
    // Validation implementation
    
    @Override
    public boolean isValid() {
        return getValidationErrors().length == 0;
    }
    
    @Override
    public String[] getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        // Required field validation
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            errors.add("Appointment ID is required");
        }
        
        if (patientId == null || patientId.trim().isEmpty()) {
            errors.add("Patient ID is required");
        }
        
        if (doctorId == null || doctorId.trim().isEmpty()) {
            errors.add("Doctor ID is required");
        }
        
        if (baseAmount < 0) {
            errors.add("Base amount cannot be negative");
        }
        
        if (totalAmount < 0) {
            errors.add("Total amount cannot be negative");
        }
        
        if (billType == null || billType.trim().isEmpty()) {
            errors.add("Bill type is required");
        } else {
            String[] validTypes = {"CONSULTATION", "SURGERY", "EMERGENCY", "MEDICATION", "DIAGNOSTIC", "FOLLOW_UP"};
            boolean validType = Arrays.asList(validTypes).contains(billType.toUpperCase());
            if (!validType) {
                errors.add("Bill type must be one of: " + String.join(", ", validTypes));
            }
        }
        
        if (dueDate != null && dueDate.isBefore(getCreatedAt())) {
            errors.add("Due date cannot be before bill creation date");
        }
        
        if (isPaid) {
            if (paymentDateTime == null) {
                errors.add("Payment date is required for paid bills");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                errors.add("Payment method is required for paid bills");
            }
        }
        
        if (insuranceCoverage < 0) {
            errors.add("Insurance coverage cannot be negative");
        }
        
        if (insuranceCoverage > getBaseAmount()) {
            errors.add("Insurance coverage cannot exceed base amount");
        }
        
        return errors.toArray(new String[0]);
    }
    
    @Override
    public String getEntityType() {
        return "Bill";
    }
    
    @Override
    public String getDisplayName() {
        return String.format("Bill %s (₹%.2f)", getId(), totalAmount);
    }
    
    // Bill-specific methods
    
    /**
     * Applies insurance coverage to the bill.
     * 
     * @param claimId the insurance claim ID
     * @param coverageAmount the coverage amount
     */
    public void applyInsuranceCoverage(String claimId, double coverageAmount) {
        if (coverageAmount < 0) {
            throw new IllegalArgumentException("Coverage amount cannot be negative");
        }
        
        this.insuranceClaimId = claimId;
        this.insuranceCoverage = Math.min(coverageAmount, getBaseAmount());
        calculateAmounts();
        updateTimestamp();
    }
    
    /**
     * Gets bill status as string.
     * 
     * @return bill status
     */
    public String getBillStatus() {
        if (isPaid) {
            return "PAID";
        } else if (isOverdue(dueDate)) {
            return "OVERDUE";
        } else {
            return "PENDING";
        }
    }
    
    /**
     * Gets days until due date.
     * 
     * @return days until due (negative if overdue)
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }
    
    /**
     * Calculates late fees if applicable.
     * 
     * @return late fee amount
     */
    public double getLateFees() {
        if (isPaid || dueDate == null) return 0.0;
        return calculateLateFee(dueDate, totalAmount * 0.01); // 1% daily late fee
    }
    
    /**
     * Gets comprehensive bill breakdown.
     * 
     * @return detailed bill breakdown
     */
    @Override
    public Map<String, Double> getPaymentBreakdown() {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        
        breakdown.put("Consultation Fee", baseAmount);
        
        double itemsTotal = billItems.stream().mapToDouble(BillItem::getTotalPrice).sum();
        if (itemsTotal > 0) {
            breakdown.put("Additional Items", itemsTotal);
        }
        
        breakdown.put("Subtotal", getBaseAmount());
        
        if (discountAmount > 0) {
            breakdown.put("Discount", -discountAmount);
        }
        
        if (insuranceCoverage > 0) {
            breakdown.put("Insurance Coverage", -insuranceCoverage);
        }
        
        breakdown.put("Amount After Discounts", getBaseAmount() - discountAmount - insuranceCoverage);
        breakdown.put("Tax (" + Constants.getTaxRatePercentage() + ")", taxAmount);
        
        double lateFees = getLateFees();
        if (lateFees > 0) {
            breakdown.put("Late Fees", lateFees);
        }
        
        breakdown.put("Total Amount", totalAmount + lateFees);
        
        return breakdown;
    }
    
    @Override
    public String toString() {
        return String.format("Bill{id='%s', patient='%s', amount=₹%.2f, status='%s'}", 
                getId(), patientId, totalAmount, getBillStatus());
    }
    
    /**
     * Gets detailed bill information for display.
     * 
     * @return detailed bill information
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=".repeat(60)).append("\n");
        info.append("MEDICAL BILL").append("\n");
        info.append("=".repeat(60)).append("\n");
        info.append("Bill ID: ").append(getId()).append("\n");
        info.append("Appointment ID: ").append(appointmentId).append("\n");
        info.append("Patient ID: ").append(patientId).append("\n");
        info.append("Doctor ID: ").append(doctorId).append("\n");
        info.append("Bill Type: ").append(billType).append("\n");
        info.append("Generated By: ").append(generatedBy != null ? generatedBy : "System").append("\n");
        info.append("Date: ").append(getCreatedAt().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT))).append("\n");
        info.append("Due Date: ").append(dueDate != null ? dueDate.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)) : "N/A").append("\n");
        info.append("\n");
        
        // Bill items breakdown
        info.append("BILL BREAKDOWN:\n");
        info.append("-".repeat(40)).append("\n");
        info.append(String.format("%-25s %10s\n", "Description", "Amount"));
        info.append("-".repeat(40)).append("\n");
        info.append(String.format("%-25s ₹%9.2f\n", "Consultation Fee", baseAmount));
        
        for (BillItem item : billItems) {
            info.append(String.format("%-25s ₹%9.2f\n", 
                    item.getDescription() + " (" + item.getQuantity() + "×" + 
                    String.format("%.2f", item.getUnitPrice()) + ")", 
                    item.getTotalPrice()));
        }
        
        info.append("-".repeat(40)).append("\n");
        
        Map<String, Double> breakdown = getPaymentBreakdown();
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            info.append(String.format("%-25s ₹%9.2f\n", entry.getKey(), entry.getValue()));
        }
        
        info.append("=".repeat(40)).append("\n");
        info.append("\n");
        
        info.append("STATUS: ").append(getBillStatus()).append("\n");
        
        if (isPaid) {
            info.append("Paid on: ").append(paymentDateTime.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT))).append("\n");
            info.append("Payment Method: ").append(paymentMethod).append("\n");
            if (paymentReference != null) {
                info.append("Reference: ").append(paymentReference).append("\n");
            }
        } else {
            long daysUntilDue = getDaysUntilDue();
            if (daysUntilDue < 0) {
                info.append("OVERDUE by ").append(-daysUntilDue).append(" days\n");
                double lateFees = getLateFees();
                if (lateFees > 0) {
                    info.append("Late Fees: ₹").append(String.format("%.2f", lateFees)).append("\n");
                }
            } else {
                info.append("Due in ").append(daysUntilDue).append(" days\n");
            }
        }
        
        if (insuranceClaimId != null) {
            info.append("Insurance Claim: ").append(insuranceClaimId).append("\n");
        }
        
        if (billNotes != null) {
            info.append("\nNotes:\n").append(billNotes).append("\n");
        }
        
        info.append("=".repeat(60)).append("\n");
        
        return info.toString();
    }
}