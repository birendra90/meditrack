package com.airtribe.meditrack.entity;

/**
 * Enum representing different medical specializations.
 * This demonstrates enum usage with methods and constructors.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public enum Specialization {
    
    CARDIOLOGY("Cardiology", "Heart specialist", 2000.0),
    NEUROLOGY("Neurology", "Brain specialist", 2500.0),
    ORTHOPEDICS("Orthopedics", "Bone specialist", 1800.0),
    DERMATOLOGY("Dermatology", "Skin specialist", 1200.0),
    PEDIATRICS("Pediatrics", "Child specialist", 1500.0),
    GENERAL_MEDICINE("General Medicine", "General doctor", 1000.0);
    
    private final String displayName;
    private final String description;
    private final double baseConsultationFee;
    
    /**
     * Constructor for Specialization enum.
     * 
     * @param displayName The display name of the specialization
     * @param description Description of what this specialization covers
     * @param baseConsultationFee Base consultation fee for this specialization
     */
    Specialization(String displayName, String description, double baseConsultationFee) {
        this.displayName = displayName;
        this.description = description;
        this.baseConsultationFee = baseConsultationFee;
    }
    
    /**
     * Gets the display name of the specialization.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the specialization.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the base consultation fee for this specialization.
     * 
     * @return The base consultation fee
     */
    public double getBaseConsultationFee() {
        return baseConsultationFee;
    }
    
    /**
     * Find specialization by name (simple version).
     *
     * @param name The name to search for
     * @return The matching specialization or GENERAL_MEDICINE if not found
     */
    public static Specialization findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return GENERAL_MEDICINE;
        }
        
        for (Specialization spec : values()) {
            if (spec.displayName.equalsIgnoreCase(name.trim()) ||
                spec.name().equalsIgnoreCase(name.trim())) {
                return spec;
            }
        }
        return GENERAL_MEDICINE; // Default fallback
    }
    
    /**
     * Calculate consultation fee based on doctor's experience.
     * Demonstrates enum methods with business logic.
     *
     * @param yearsExperience Years of experience
     * @return Calculated fee
     */
    public double calculateFee(int yearsExperience) {
        // Simple calculation: base fee + 5% per year of experience
        double multiplier = 1.0 + (yearsExperience * 0.05);
        return baseConsultationFee * multiplier;
    }
    
    /**
     * Check if this is a high-cost specialization.
     *
     * @return true if fee is above 1500
     */
    public boolean isHighCost() {
        return baseConsultationFee > 1500.0;
    }
    
    /**
     * Calculate consultation fee with experience multiplier.
     * 
     * @param yearsExperience Years of experience of the doctor
     * @return Calculated consultation fee
     */
    public double calculateConsultationFee(int yearsExperience) {
        double multiplier = 1.0 + (yearsExperience * 0.05); // 5% increase per year of experience
        return baseConsultationFee * Math.min(multiplier, 3.0); // Cap at 3x base fee
    }
    
    @Override
    public String toString() {
        return String.format("%s (₹%.2f) - %s", displayName, baseConsultationFee, description);
    }
}