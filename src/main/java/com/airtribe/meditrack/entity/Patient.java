package com.airtribe.meditrack.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Patient entity representing patients in the medical system.
 * This demonstrates inheritance, polymorphism, and cloning (deep vs shallow copy).
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class Patient extends Person implements Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    // Patient-specific fields
    private String patientType; // INPATIENT, OUTPATIENT, EMERGENCY
    private List<String> medicalHistory;
    private List<String> allergies;
    private List<String> currentMedications;
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private String guardianName; // For minors
    private String guardianPhone;
    private String preferredLanguage;
    private boolean hasInsurance;
    private double insuranceClaimLimit;
    private LocalDate registrationDate;
    private String referredBy;
    private int visitCount;
    
    // Static counter for tracking total patients
    private static int totalPatients = 0;
    
    // Static initialization block
    static {
        System.out.println("[STATIC BLOCK] Patient class initialized");
        // Initialize any static resources if needed
    }
    
    /**
     * Default constructor.
     */
    public Patient() {
        super();
        initializePatient();
    }
    
    /**
     * Simple constructor with required fields.
     */
    public Patient(String id, String firstName, String lastName, LocalDate dateOfBirth,
                  String gender, String patientType) {
        super(id, firstName, lastName, dateOfBirth, gender);
        this.patientType = patientType;
        initializePatient();
    }
    
    /**
     * Constructor with all basic fields for PatientService.
     */
    public Patient(String id, String firstName, String lastName, LocalDate dateOfBirth,
                  String gender, String email, String phone, String address,
                  String patientType, String insuranceProvider) {
        super(id, firstName, lastName, dateOfBirth, gender, email, phone, address);
        this.patientType = patientType;
        this.insuranceProvider = insuranceProvider;
        this.hasInsurance = (insuranceProvider != null && !insuranceProvider.trim().isEmpty());
        initializePatient();
    }
    
    /**
     * Copy constructor for cloning.
     * 
     * @param original the original patient to copy from
     */
    private Patient(Patient original) {
        super();
        copyFromOriginal(original);
    }
    
    /**
     * Initializes patient-specific defaults.
     */
    private void initializePatient() {
        this.medicalHistory = new ArrayList<>();
        this.allergies = new ArrayList<>();
        this.currentMedications = new ArrayList<>();
        this.preferredLanguage = "English";
        this.registrationDate = LocalDate.now();
        this.visitCount = 0;
        this.insuranceClaimLimit = 0.0;
        
        synchronized (Patient.class) {
            totalPatients++;
        }
    }
    
    /**
     * Helper method for copying data from original patient.
     * Used in cloning operations.
     * 
     * @param original the original patient
     */
    private void copyFromOriginal(Patient original) {
        // Copy basic fields
        this.setId(original.getId());
        this.setFirstName(original.getFirstName());
        this.setLastName(original.getLastName());
        this.setDateOfBirth(original.getDateOfBirth());
        this.setGender(original.getGender());
        this.setEmail(original.getEmail());
        this.setPhone(original.getPhone());
        this.setAddress(original.getAddress());
        this.setEmergencyContact(original.getEmergencyContact());
        this.setEmergencyPhone(original.getEmergencyPhone());
        this.setBloodGroup(original.getBloodGroup());
        
        // Copy patient-specific fields
        this.patientType = original.patientType;
        this.insuranceProvider = original.insuranceProvider;
        this.insurancePolicyNumber = original.insurancePolicyNumber;
        this.guardianName = original.guardianName;
        this.guardianPhone = original.guardianPhone;
        this.preferredLanguage = original.preferredLanguage;
        this.hasInsurance = original.hasInsurance;
        this.insuranceClaimLimit = original.insuranceClaimLimit;
        this.registrationDate = original.registrationDate;
        this.referredBy = original.referredBy;
        this.visitCount = original.visitCount;
        
        // Deep copy of collections (important for proper cloning)
        this.medicalHistory = new ArrayList<>(original.medicalHistory);
        this.allergies = new ArrayList<>(original.allergies);
        this.currentMedications = new ArrayList<>(original.currentMedications);
    }
    
    // Getters and Setters
    
    public String getPatientType() {
        return patientType;
    }
    
    public void setPatientType(String patientType) {
        this.patientType = patientType;
        updateTimestamp();
    }
    
    public List<String> getMedicalHistory() {
        return new ArrayList<>(medicalHistory); // Return defensive copy
    }
    
    public void setMedicalHistory(List<String> medicalHistory) {
        this.medicalHistory = new ArrayList<>(medicalHistory);
        updateTimestamp();
    }
    
    public void addMedicalHistory(String historyItem) {
        this.medicalHistory.add(historyItem);
        updateTimestamp();
    }
    
    public List<String> getAllergies() {
        return new ArrayList<>(allergies); // Return defensive copy
    }
    
    public void setAllergies(List<String> allergies) {
        this.allergies = new ArrayList<>(allergies);
        updateTimestamp();
    }
    
    public void addAllergy(String allergy) {
        if (!this.allergies.contains(allergy)) {
            this.allergies.add(allergy);
            updateTimestamp();
        }
    }
    
    public List<String> getCurrentMedications() {
        return new ArrayList<>(currentMedications); // Return defensive copy
    }
    
    public void setCurrentMedications(List<String> currentMedications) {
        this.currentMedications = new ArrayList<>(currentMedications);
        updateTimestamp();
    }
    
    public void addCurrentMedication(String medication) {
        if (!this.currentMedications.contains(medication)) {
            this.currentMedications.add(medication);
            updateTimestamp();
        }
    }
    
    public void removeMedication(String medication) {
        this.currentMedications.remove(medication);
        updateTimestamp();
    }
    
    public String getInsuranceProvider() {
        return insuranceProvider;
    }
    
    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
        this.hasInsurance = insuranceProvider != null && !insuranceProvider.trim().isEmpty();
        updateTimestamp();
    }
    
    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }
    
    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
        updateTimestamp();
    }
    
    public String getGuardianName() {
        return guardianName;
    }
    
    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
        updateTimestamp();
    }
    
    public String getGuardianPhone() {
        return guardianPhone;
    }
    
    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
        updateTimestamp();
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
        updateTimestamp();
    }
    
    public boolean hasInsurance() {
        return hasInsurance;
    }
    
    public double getInsuranceClaimLimit() {
        return insuranceClaimLimit;
    }
    
    public void setInsuranceClaimLimit(double insuranceClaimLimit) {
        this.insuranceClaimLimit = insuranceClaimLimit;
        updateTimestamp();
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
        updateTimestamp();
    }
    
    public String getReferredBy() {
        return referredBy;
    }
    
    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
        updateTimestamp();
    }
    
    public int getVisitCount() {
        return visitCount;
    }
    
    public void incrementVisitCount() {
        this.visitCount++;
        updateTimestamp();
    }
    
    // Static method to get total patients count
    public static int getTotalPatients() {
        return totalPatients;
    }
    
    // Simple Cloneable implementation for deep copying
    
    /**
     * Creates a deep copy of this patient.
     * Demonstrates deep vs shallow copying concept.
     *
     * @return a deep clone of this patient
     * @throws CloneNotSupportedException if cloning fails
     */
    @Override
    public Patient clone() throws CloneNotSupportedException {
        // Step 1: Call superclass clone (creates shallow copy)
        Patient cloned = (Patient) super.clone();
        
        // Step 2: Generate new ID for the clone (important for deep copy testing)
        cloned.setId(this.getId() + "_CLONE_" + System.currentTimeMillis());
        
        // Step 3: Deep copy the mutable collections
        // This prevents changes to original affecting the clone
        cloned.medicalHistory = new ArrayList<>(this.medicalHistory);
        cloned.allergies = new ArrayList<>(this.allergies);
        cloned.currentMedications = new ArrayList<>(this.currentMedications);
        
        return cloned;
    }
    
    // Override methods from Person class
    
    @Override
    public List<String> getSearchableTerms() {
        List<String> terms = super.getSearchableTerms();
        
        if (patientType != null) terms.add(patientType);
        if (insuranceProvider != null) terms.add(insuranceProvider);
        if (insurancePolicyNumber != null) terms.add(insurancePolicyNumber);
        if (guardianName != null) terms.add(guardianName);
        if (preferredLanguage != null) terms.add(preferredLanguage);
        if (referredBy != null) terms.add(referredBy);
        
        // Add medical information to searchable terms
        terms.addAll(medicalHistory);
        terms.addAll(allergies);
        
        return terms;
    }
    
    @Override
    public String[] getValidationErrors() {
        List<String> errors = new ArrayList<>(Arrays.asList(super.getValidationErrors()));
        
        // Patient-specific validations
        if (patientType == null || patientType.trim().isEmpty()) {
            errors.add("Patient type is required");
        } else {
            String[] validTypes = {"INPATIENT", "OUTPATIENT", "EMERGENCY"};
            boolean validType = Arrays.asList(validTypes).contains(patientType.toUpperCase());
            if (!validType) {
                errors.add("Patient type must be one of: " + String.join(", ", validTypes));
            }
        }
        
        // Guardian validation for minors
        if (isMinor()) {
            if (guardianName == null || guardianName.trim().isEmpty()) {
                errors.add("Guardian name is required for minors");
            }
            if (guardianPhone == null || guardianPhone.trim().isEmpty()) {
                errors.add("Guardian phone is required for minors");
            }
        }
        
        // Insurance validation
        if (hasInsurance) {
            if (insuranceProvider == null || insuranceProvider.trim().isEmpty()) {
                errors.add("Insurance provider is required when hasInsurance is true");
            }
            if (insurancePolicyNumber == null || insurancePolicyNumber.trim().isEmpty()) {
                errors.add("Insurance policy number is required when hasInsurance is true");
            }
        }
        
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.add("Registration date cannot be in the future");
        }
        
        if (visitCount < 0) {
            errors.add("Visit count cannot be negative");
        }
        
        return errors.toArray(new String[0]);
    }
    
    @Override
    public String getEntityType() {
        return "Patient";
    }
    
    @Override
    public String getDisplayName() {
        return getFullName() + " (" + patientType + ")";
    }
    
    // Patient-specific methods
    
    /**
     * Checks if patient has any allergies.
     * 
     * @return true if patient has allergies
     */
    public boolean hasAllergies() {
        return !allergies.isEmpty();
    }
    
    /**
     * Checks if patient is allergic to specific substance.
     * 
     * @param substance the substance to check
     * @return true if allergic
     */
    public boolean isAllergicTo(String substance) {
        return allergies.stream()
                .anyMatch(allergy -> allergy.toLowerCase().contains(substance.toLowerCase()));
    }
    
    /**
     * Checks if patient is currently taking specific medication.
     * 
     * @param medication the medication to check
     * @return true if currently taking
     */
    public boolean isCurrentlyTaking(String medication) {
        return currentMedications.stream()
                .anyMatch(med -> med.toLowerCase().contains(medication.toLowerCase()));
    }
    
    /**
     * Gets patient priority based on age and patient type.
     * 
     * @return priority level (HIGH, MEDIUM, LOW)
     */
    public String getPriority() {
        if ("EMERGENCY".equals(patientType)) {
            return "HIGH";
        }
        
        if (isSeniorCitizen() || isMinor()) {
            return "MEDIUM";
        }
        
        return "LOW";
    }
    
    /**
     * Calculates years since registration.
     * 
     * @return years as a patient
     */
    public int getYearsAsPatient() {
        if (registrationDate == null) {
            return 0;
        }
        return LocalDate.now().getYear() - registrationDate.getYear();
    }
    
    /**
     * Checks if patient is a regular (multiple visits).
     * 
     * @return true if regular patient
     */
    public boolean isRegularPatient() {
        return visitCount >= 3;
    }
    
    /**
     * Gets patient status summary.
     * 
     * @return status summary string
     */
    public String getPatientStatus() {
        StringBuilder status = new StringBuilder();
        
        status.append("Type: ").append(patientType);
        status.append(", Priority: ").append(getPriority());
        status.append(", Visits: ").append(visitCount);
        
        if (hasInsurance) {
            status.append(", Insured");
        }
        
        if (hasAllergies()) {
            status.append(", Has Allergies");
        }
        
        if (!currentMedications.isEmpty()) {
            status.append(", On Medication");
        }
        
        return status.toString();
    }
    
    /**
     * Gets comprehensive medical summary.
     * 
     * @return medical summary string
     */
    public String getMedicalSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Medical Summary for ").append(getFullName()).append(":\n");
        summary.append("-".repeat(40)).append("\n");
        
        if (!medicalHistory.isEmpty()) {
            summary.append("Medical History:\n");
            for (String history : medicalHistory) {
                summary.append("  • ").append(history).append("\n");
            }
        }
        
        if (!allergies.isEmpty()) {
            summary.append("Allergies:\n");
            for (String allergy : allergies) {
                summary.append("  • ").append(allergy).append("\n");
            }
        }
        
        if (!currentMedications.isEmpty()) {
            summary.append("Current Medications:\n");
            for (String medication : currentMedications) {
                summary.append("  • ").append(medication).append("\n");
            }
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Patient{id='%s', name='%s', type='%s', visits=%d, hasInsurance=%s}", 
                getId(), getFullName(), patientType, visitCount, hasInsurance);
    }
    
    /**
     * Gets detailed patient information for display.
     * 
     * @return detailed patient information
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=".repeat(50)).append("\n");
        info.append("PATIENT INFORMATION").append("\n");
        info.append("=".repeat(50)).append("\n");
        info.append(toDetailedString());
        info.append("Patient Type: ").append(patientType).append("\n");
        info.append("Priority: ").append(getPriority()).append("\n");
        info.append("Registration Date: ").append(registrationDate).append("\n");
        info.append("Visit Count: ").append(visitCount).append("\n");
        info.append("Years as Patient: ").append(getYearsAsPatient()).append("\n");
        
        if (hasInsurance) {
            info.append("Insurance Provider: ").append(insuranceProvider).append("\n");
            info.append("Policy Number: ").append(insurancePolicyNumber).append("\n");
            info.append("Claim Limit: ₹").append(String.format("%.2f", insuranceClaimLimit)).append("\n");
        }
        
        if (isMinor() && guardianName != null) {
            info.append("Guardian: ").append(guardianName);
            if (guardianPhone != null) {
                info.append(" (").append(guardianPhone).append(")");
            }
            info.append("\n");
        }
        
        info.append("Preferred Language: ").append(preferredLanguage).append("\n");
        
        if (referredBy != null) {
            info.append("Referred By: ").append(referredBy).append("\n");
        }
        
        info.append("\n").append(getMedicalSummary());
        info.append("=".repeat(50)).append("\n");
        
        return info.toString();
    }
}