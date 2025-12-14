package com.airtribe.meditrack.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Doctor entity representing medical practitioners in the system.
 * This demonstrates inheritance, polymorphism, and method overriding.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class Doctor extends Person {
    
    private static final long serialVersionUID = 1L;
    
    // Doctor-specific fields
    private String licenseNumber;
    private Specialization specialization;
    private int yearsOfExperience;
    private double consultationFee;
    private String qualification;
    private String department;
    private boolean isAvailable;
    private List<String> workingDays;
    private String workingHours;
    private String chamber;
    private double rating;
    private int totalPatientsTreated;
    
    // Static counter for tracking total doctors
    private static int totalDoctors = 0;
    
    // Static initialization block
    static {
        System.out.println("[STATIC BLOCK] Doctor class initialized");
    }
    
    /**
     * Default constructor.
     */
    public Doctor() {
        super();
        initializeDoctor();
    }
    
    /**
     * Constructor with basic information.
     */
    public Doctor(String id, String firstName, String lastName, LocalDate dateOfBirth, 
                 String gender, String licenseNumber, Specialization specialization) {
        super(id, firstName, lastName, dateOfBirth, gender);
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        initializeDoctor();
    }
    
    /**
     * Constructor with complete information.
     */
    public Doctor(String id, String firstName, String lastName, LocalDate dateOfBirth, 
                 String gender, String email, String phone, String address,
                 String licenseNumber, Specialization specialization, int yearsOfExperience,
                 String qualification) {
        super(id, firstName, lastName, dateOfBirth, gender, email, phone, address);
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.yearsOfExperience = yearsOfExperience;
        this.qualification = qualification;
        initializeDoctor();
    }
    
    /**
     * Initializes doctor-specific defaults.
     * This demonstrates constructor chaining and initialization.
     */
    private void initializeDoctor() {
        this.isAvailable = true;
        this.workingDays = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        this.workingHours = "09:00-18:00";
        this.rating = 0.0;
        this.totalPatientsTreated = 0;
        
        // Calculate consultation fee based on specialization and experience
        if (specialization != null) {
            this.consultationFee = specialization.calculateConsultationFee(yearsOfExperience);
        }
        
        synchronized (Doctor.class) {
            totalDoctors++;
        }
    }
    
    // Getters and Setters
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        updateTimestamp();
    }
    
    public Specialization getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
        // Recalculate consultation fee when specialization changes
        if (specialization != null) {
            this.consultationFee = specialization.calculateConsultationFee(yearsOfExperience);
        }
        updateTimestamp();
    }
    
    public int getYearsOfExperience() {
        return yearsOfExperience;
    }
    
    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
        // Recalculate consultation fee when experience changes
        if (specialization != null) {
            this.consultationFee = specialization.calculateConsultationFee(yearsOfExperience);
        }
        updateTimestamp();
    }
    
    public double getConsultationFee() {
        return consultationFee;
    }
    
    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        updateTimestamp();
    }
    
    public String getQualification() {
        return qualification;
    }
    
    public void setQualification(String qualification) {
        this.qualification = qualification;
        updateTimestamp();
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
        updateTimestamp();
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
        updateTimestamp();
    }
    
    public List<String> getWorkingDays() {
        return new ArrayList<>(workingDays); // Return defensive copy
    }
    
    public void setWorkingDays(List<String> workingDays) {
        this.workingDays = new ArrayList<>(workingDays);
        updateTimestamp();
    }
    
    public String getWorkingHours() {
        return workingHours;
    }
    
    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
        updateTimestamp();
    }
    
    public String getChamber() {
        return chamber;
    }
    
    public void setChamber(String chamber) {
        this.chamber = chamber;
        updateTimestamp();
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(5.0, rating)); // Clamp between 0 and 5
        updateTimestamp();
    }
    
    public int getTotalPatientsTreated() {
        return totalPatientsTreated;
    }
    
    public void incrementPatientsTreated() {
        this.totalPatientsTreated++;
        updateTimestamp();
    }
    
    // Static method to get total doctors count
    public static int getTotalDoctors() {
        return totalDoctors;
    }
    
    // Override methods from Person class (demonstrating polymorphism)
    
    @Override
    public List<String> getSearchableTerms() {
        List<String> terms = super.getSearchableTerms();
        
        if (licenseNumber != null) terms.add(licenseNumber);
        if (specialization != null) {
            terms.add(specialization.name());
            terms.add(specialization.getDisplayName());
        }
        if (qualification != null) terms.add(qualification);
        if (department != null) terms.add(department);
        if (chamber != null) terms.add(chamber);
        
        return terms;
    }
    
    @Override
    public String[] getValidationErrors() {
        List<String> errors = new ArrayList<>(Arrays.asList(super.getValidationErrors()));
        
        // Doctor-specific validations
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            errors.add("License number is required for doctors");
        }
        
        if (specialization == null) {
            errors.add("Specialization is required for doctors");
        }
        
        if (yearsOfExperience < 0 || yearsOfExperience > 60) {
            errors.add("Years of experience must be between 0 and 60");
        }
        
        if (consultationFee < 0) {
            errors.add("Consultation fee cannot be negative");
        }
        
        if (qualification == null || qualification.trim().isEmpty()) {
            errors.add("Medical qualification is required");
        }
        
        // Validate working days
        if (workingDays == null || workingDays.isEmpty()) {
            errors.add("At least one working day must be specified");
        }
        
        return errors.toArray(new String[0]);
    }
    
    @Override
    public String getEntityType() {
        return "Doctor";
    }
    
    @Override
    public String getDisplayName() {
        String prefix = getAge() >= 35 || yearsOfExperience >= 10 ? "Dr. " : "Dr. ";
        return prefix + getFullName() + 
               (specialization != null ? " (" + specialization.getDisplayName() + ")" : "");
    }
    
    // Doctor-specific methods
    
    /**
     * Calculates effective consultation fee including experience bonus.
     * Demonstrates method overloading.
     * 
     * @return effective consultation fee
     */
    public double getEffectiveConsultationFee() {
        return getEffectiveConsultationFee(false);
    }
    
    /**
     * Calculates effective consultation fee with optional emergency surcharge.
     * Demonstrates method overloading.
     * 
     * @param isEmergency whether this is an emergency consultation
     * @return effective consultation fee
     */
    public double getEffectiveConsultationFee(boolean isEmergency) {
        double fee = consultationFee;
        
        if (isEmergency) {
            fee *= 1.5; // 50% surcharge for emergency
        }
        
        // Reputation bonus based on rating
        if (rating >= 4.5) {
            fee *= 1.2; // 20% premium for highly rated doctors
        } else if (rating >= 4.0) {
            fee *= 1.1; // 10% premium for well-rated doctors
        }
        
        return fee;
    }
    
    /**
     * Checks if doctor is available on a specific day.
     * 
     * @param day the day to check
     * @return true if available on that day
     */
    public boolean isAvailableOnDay(String day) {
        return isAvailable && workingDays.contains(day);
    }
    
    /**
     * Gets the experience level as a string.
     * 
     * @return experience level description
     */
    public String getExperienceLevel() {
        if (yearsOfExperience < 2) {
            return "Junior";
        } else if (yearsOfExperience < 10) {
            return "Mid-level";
        } else if (yearsOfExperience < 20) {
            return "Senior";
        } else {
            return "Expert";
        }
    }
    
    /**
     * Checks if doctor is senior (based on experience).
     * 
     * @return true if senior doctor
     */
    public boolean isSeniorDoctor() {
        return yearsOfExperience >= 10;
    }
    
    /**
     * Gets professional title based on experience and qualifications.
     * 
     * @return professional title
     */
    public String getProfessionalTitle() {
        StringBuilder title = new StringBuilder("Dr.");
        
        if (yearsOfExperience >= 20) {
            title.append(" Prof.");
        } else if (yearsOfExperience >= 15) {
            title.append(" Sr.");
        }
        
        return title.toString();
    }
    
    /**
     * Calculate monthly earning potential based on consultation fee and capacity.
     * 
     * @param consultationsPerDay average consultations per day
     * @return estimated monthly earnings
     */
    public double calculateMonthlyEarnings(int consultationsPerDay) {
        int workingDaysPerMonth = workingDays.size() * 4; // Approximate
        return getEffectiveConsultationFee() * consultationsPerDay * workingDaysPerMonth;
    }
    
    /**
     * Gets doctor availability status.
     * 
     * @return availability status string
     */
    public String getAvailabilityStatus() {
        if (!isAvailable) {
            return "Unavailable";
        }
        
        String today = java.time.LocalDate.now().getDayOfWeek().toString();
        today = today.charAt(0) + today.substring(1).toLowerCase();
        
        if (isAvailableOnDay(today)) {
            return "Available Today";
        } else {
            return "Available (Not Today)";
        }
    }
    
    /**
     * Updates doctor rating based on new feedback.
     * 
     * @param newRating the new rating to incorporate
     * @param totalRatings total number of ratings received
     */
    public void updateRating(double newRating, int totalRatings) {
        if (totalRatings <= 1) {
            setRating(newRating);
        } else {
            double updatedRating = ((rating * (totalRatings - 1)) + newRating) / totalRatings;
            setRating(updatedRating);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Doctor{id='%s', name='%s', specialization='%s', experience=%d years, fee=₹%.2f}", 
                getId(), getFullName(), 
                specialization != null ? specialization.getDisplayName() : "Unknown",
                yearsOfExperience, consultationFee);
    }
    
    /**
     * Gets detailed doctor information for display.
     * 
     * @return detailed doctor information
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=".repeat(50)).append("\n");
        info.append("DOCTOR INFORMATION").append("\n");
        info.append("=".repeat(50)).append("\n");
        info.append(toDetailedString());
        info.append("License: ").append(licenseNumber).append("\n");
        info.append("Specialization: ").append(specialization != null ? specialization.getDisplayName() : "N/A").append("\n");
        info.append("Experience: ").append(yearsOfExperience).append(" years (").append(getExperienceLevel()).append(")\n");
        info.append("Qualification: ").append(qualification != null ? qualification : "N/A").append("\n");
        info.append("Department: ").append(department != null ? department : "N/A").append("\n");
        info.append("Consultation Fee: ₹").append(String.format("%.2f", consultationFee)).append("\n");
        info.append("Rating: ").append(String.format("%.1f", rating)).append("/5.0\n");
        info.append("Patients Treated: ").append(totalPatientsTreated).append("\n");
        info.append("Chamber: ").append(chamber != null ? chamber : "N/A").append("\n");
        info.append("Working Days: ").append(String.join(", ", workingDays)).append("\n");
        info.append("Working Hours: ").append(workingHours).append("\n");
        info.append("Status: ").append(getAvailabilityStatus()).append("\n");
        info.append("=".repeat(50)).append("\n");
        
        return info.toString();
    }
}