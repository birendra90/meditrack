package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Specialization;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.function.Predicate;

/**
 * Service class for managing Doctor entities and operations.
 * This demonstrates business logic layer, CRUD operations, and service patterns.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class DoctorService {
    
    private final DataStore<Doctor> doctorStore;
    private final IdGenerator idGenerator;
    
    // Predefined comparators for different sorting needs
    public static final Comparator<Doctor> BY_NAME = 
            Comparator.comparing(Doctor::getLastName)
                     .thenComparing(Doctor::getFirstName);
    
    public static final Comparator<Doctor> BY_SPECIALIZATION = 
            Comparator.comparing(doctor -> doctor.getSpecialization() != null ? 
                                         doctor.getSpecialization().getDisplayName() : "");
    
    public static final Comparator<Doctor> BY_EXPERIENCE = 
            Comparator.comparing(Doctor::getYearsOfExperience).reversed();
    
    public static final Comparator<Doctor> BY_RATING = 
            Comparator.comparing(Doctor::getRating).reversed();
    
    public static final Comparator<Doctor> BY_CONSULTATION_FEE = 
            Comparator.comparing(Doctor::getConsultationFee);
    
    /**
     * Constructor that initializes the service with a data store.
     */
    public DoctorService() {
        this.doctorStore = new DataStore<>("Doctor");
        this.idGenerator = IdGenerator.getInstance();
        
        // Set default comparator for the store
        this.doctorStore.setDefaultComparator(BY_NAME);
        
        // Initialize with some sample data for demonstration
        initializeSampleData();
    }
    
    /**
     * Constructor with external data store (for dependency injection).
     * 
     * @param doctorStore the data store to use
     */
    public DoctorService(DataStore<Doctor> doctorStore) {
        this.doctorStore = doctorStore;
        this.idGenerator = IdGenerator.getInstance();
        this.doctorStore.setDefaultComparator(BY_NAME);
    }
    
    // CRUD Operations
    
    /**
     * Creates a new doctor.
     * 
     * @param firstName the first name
     * @param lastName the last name
     * @param dateOfBirth the date of birth
     * @param gender the gender
     * @param email the email address
     * @param phone the phone number
     * @param address the address
     * @param licenseNumber the medical license number
     * @param specialization the medical specialization
     * @param yearsOfExperience the years of experience
     * @param qualification the medical qualification
     * @return the created doctor
     * @throws InvalidDataException if validation fails
     */
    public Doctor createDoctor(String firstName, String lastName, LocalDate dateOfBirth,
                              String gender, String email, String phone, String address,
                              String licenseNumber, Specialization specialization,
                              int yearsOfExperience, String qualification) throws InvalidDataException {
        
        // Validate input data
        List<String> errors = Validator.validateDoctor(
                firstName, lastName, dateOfBirth, gender, email, phone,
                licenseNumber, yearsOfExperience, 0.0 // Fee will be calculated based on specialization
        );
        
        if (!errors.isEmpty()) {
            throw new InvalidDataException("Doctor validation failed: " + String.join(", ", errors));
        }
        
        // Check for duplicate license number
        if (isDuplicateLicenseNumber(licenseNumber)) {
            throw new InvalidDataException("Doctor with license number '" + licenseNumber + "' already exists");
        }
        
        // Generate new ID
        String doctorId = idGenerator.generateDoctorId();
        
        // Create doctor entity
        Doctor doctor = new Doctor(doctorId, firstName, lastName, dateOfBirth, gender,
                                 email, phone, address, licenseNumber, specialization,
                                 yearsOfExperience, qualification);
        
        // Store the doctor
        doctorStore.store(doctorId, doctor);
        
        return doctor;
    }
    
    /**
     * Retrieves a doctor by ID.
     * 
     * @param doctorId the doctor ID
     * @return the doctor, or null if not found
     */
    public Doctor getDoctorById(String doctorId) {
        return doctorStore.get(doctorId);
    }
    
    /**
     * Updates an existing doctor.
     * 
     * @param doctorId the doctor ID
     * @param updatedDoctor the updated doctor data
     * @return the updated doctor
     * @throws InvalidDataException if validation fails or doctor not found
     */
    public Doctor updateDoctor(String doctorId, Doctor updatedDoctor) throws InvalidDataException {
        Doctor existingDoctor = doctorStore.get(doctorId);
        if (existingDoctor == null) {
            throw new InvalidDataException("Doctor with ID '" + doctorId + "' not found");
        }
        
        // Validate the updated doctor
        updatedDoctor.validateAndThrow();
        
        // Check for duplicate license number (excluding current doctor)
        if (!existingDoctor.getLicenseNumber().equals(updatedDoctor.getLicenseNumber()) &&
            isDuplicateLicenseNumber(updatedDoctor.getLicenseNumber())) {
            throw new InvalidDataException("Another doctor with license number '" + 
                                         updatedDoctor.getLicenseNumber() + "' already exists");
        }
        
        // Ensure ID remains the same
        updatedDoctor.setId(doctorId);
        
        // Update in store
        doctorStore.update(doctorId, updatedDoctor);
        
        return updatedDoctor;
    }
    
    /**
     * Deletes a doctor by ID.
     * 
     * @param doctorId the doctor ID
     * @return true if deleted, false if not found
     */
    public boolean deleteDoctor(String doctorId) {
        Doctor removed = doctorStore.remove(doctorId);
        return removed != null;
    }
    
    /**
     * Soft deletes a doctor (marks as inactive).
     * 
     * @param doctorId the doctor ID
     * @return true if deactivated, false if not found
     */
    public boolean deactivateDoctor(String doctorId) {
        Doctor doctor = doctorStore.get(doctorId);
        if (doctor != null) {
            doctor.setActive(false);
            return true;
        }
        return false;
    }
    
    // Query Operations
    
    /**
     * Gets all doctors.
     * 
     * @return list of all doctors
     */
    public List<Doctor> getAllDoctors() {
        return doctorStore.getAll().stream()
                .filter(Doctor::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all doctors sorted by name.
     * 
     * @return sorted list of doctors
     */
    public List<Doctor> getAllDoctorsSorted() {
        return doctorStore.getAllSorted(BY_NAME).stream()
                .filter(Doctor::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets doctors sorted by a specific criteria.
     * 
     * @param comparator the comparator to use for sorting
     * @return sorted list of doctors
     */
    public List<Doctor> getAllDoctorsSorted(Comparator<Doctor> comparator) {
        return doctorStore.getAllSorted(comparator).stream()
                .filter(Doctor::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds doctors by specialization.
     * 
     * @param specialization the specialization to search for
     * @return list of matching doctors
     */
    public List<Doctor> findDoctorsBySpecialization(Specialization specialization) {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() && 
                doctor.getSpecialization() == specialization);
    }
    
    /**
     * Finds available doctors by specialization.
     * 
     * @param specialization the specialization to search for
     * @return list of available doctors
     */
    public List<Doctor> findAvailableDoctorsBySpecialization(Specialization specialization) {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() && 
                doctor.isAvailable() &&
                doctor.getSpecialization() == specialization);
    }
    
    /**
     * Finds doctors by experience range.
     * 
     * @param minYears minimum years of experience
     * @param maxYears maximum years of experience
     * @return list of matching doctors
     */
    public List<Doctor> findDoctorsByExperience(int minYears, int maxYears) {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() &&
                doctor.getYearsOfExperience() >= minYears && 
                doctor.getYearsOfExperience() <= maxYears);
    }
    
    /**
     * Finds doctors by consultation fee range.
     * 
     * @param minFee minimum consultation fee
     * @param maxFee maximum consultation fee
     * @return list of matching doctors
     */
    public List<Doctor> findDoctorsByFeeRange(double minFee, double maxFee) {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() &&
                doctor.getConsultationFee() >= minFee && 
                doctor.getConsultationFee() <= maxFee);
    }
    
    /**
     * Finds doctors by rating threshold.
     * 
     * @param minRating minimum rating
     * @return list of doctors with rating >= minRating
     */
    public List<Doctor> findDoctorsByRating(double minRating) {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() &&
                doctor.getRating() >= minRating);
    }
    
    /**
     * Finds senior doctors (experience >= 10 years).
     * 
     * @return list of senior doctors
     */
    public List<Doctor> findSeniorDoctors() {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() && doctor.isSeniorDoctor());
    }
    
    /**
     * Finds doctors available on a specific day.
     * 
     * @param day the day of the week
     * @return list of available doctors
     */
    public List<Doctor> findDoctorsAvailableOnDay(String day) {
        return doctorStore.findWhere(doctor -> 
                doctor.isActive() && 
                doctor.isAvailableOnDay(day));
    }
    
    // Search Operations
    
    /**
     * Searches doctors using various criteria.
     * 
     * @param searchTerm the search term
     * @return list of matching doctors sorted by relevance
     */
    public List<Doctor> searchDoctors(String searchTerm) {
        return doctorStore.searchAndSort(searchTerm).stream()
                .filter(Doctor::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Advanced search with multiple criteria.
     * 
     * @param name doctor name (partial match)
     * @param specialization specialization filter (optional)
     * @param minExperience minimum experience (optional)
     * @param maxFee maximum fee (optional)
     * @param minRating minimum rating (optional)
     * @param availableOnly whether to include only available doctors
     * @return list of matching doctors
     */
    public List<Doctor> advancedSearch(String name, Specialization specialization, 
                                     Integer minExperience, Double maxFee, 
                                     Double minRating, boolean availableOnly) {
        
        Predicate<Doctor> criteria = doctor -> doctor.isActive();
        
        // Add name filter
        if (name != null && !name.trim().isEmpty()) {
            String searchName = name.toLowerCase().trim();
            criteria = criteria.and(doctor -> 
                    doctor.getFullName().toLowerCase().contains(searchName));
        }
        
        // Add specialization filter
        if (specialization != null) {
            criteria = criteria.and(doctor -> doctor.getSpecialization() == specialization);
        }
        
        // Add experience filter
        if (minExperience != null) {
            criteria = criteria.and(doctor -> doctor.getYearsOfExperience() >= minExperience);
        }
        
        // Add fee filter
        if (maxFee != null) {
            criteria = criteria.and(doctor -> doctor.getConsultationFee() <= maxFee);
        }
        
        // Add rating filter
        if (minRating != null) {
            criteria = criteria.and(doctor -> doctor.getRating() >= minRating);
        }
        
        // Add availability filter
        if (availableOnly) {
            criteria = criteria.and(Doctor::isAvailable);
        }
        
        return doctorStore.findWhere(criteria);
    }
    
    // Business Logic Methods
    
    /**
     * Gets recommended doctors for a patient based on symptoms or condition.
     * This is a simple rule-based recommendation system.
     * 
     * @param symptoms the patient's symptoms
     * @return list of recommended doctors
     */
    public List<Doctor> getRecommendedDoctors(String symptoms) {
        if (symptoms == null || symptoms.trim().isEmpty()) {
            return getAllDoctorsSorted(BY_RATING);
        }
        
        String lowerSymptoms = symptoms.toLowerCase();
        Specialization recommendedSpec = null;
        
        // Simple rule-based matching
        if (lowerSymptoms.contains("heart") || lowerSymptoms.contains("chest") || 
            lowerSymptoms.contains("cardiac")) {
            recommendedSpec = Specialization.CARDIOLOGY;
        } else if (lowerSymptoms.contains("skin") || lowerSymptoms.contains("rash") || 
                   lowerSymptoms.contains("allergy")) {
            recommendedSpec = Specialization.DERMATOLOGY;
        } else if (lowerSymptoms.contains("bone") || lowerSymptoms.contains("joint") || 
                   lowerSymptoms.contains("fracture")) {
            recommendedSpec = Specialization.ORTHOPEDICS;
        } else if (lowerSymptoms.contains("eye") || lowerSymptoms.contains("vision")) {
            recommendedSpec = Specialization.GENERAL_MEDICINE;
        } else if (lowerSymptoms.contains("mental") || lowerSymptoms.contains("depression") ||
                   lowerSymptoms.contains("anxiety")) {
            recommendedSpec = Specialization.GENERAL_MEDICINE;
        }
        
        if (recommendedSpec != null) {
            return findAvailableDoctorsBySpecialization(recommendedSpec).stream()
                    .sorted(BY_RATING)
                    .collect(Collectors.toList());
        }
        
        // Default to general medicine or highest rated doctors
        List<Doctor> generalDoctors = findAvailableDoctorsBySpecialization(Specialization.GENERAL_MEDICINE);
        if (!generalDoctors.isEmpty()) {
            return generalDoctors.stream()
                    .sorted(BY_RATING)
                    .collect(Collectors.toList());
        }
        
        return getAllDoctorsSorted(BY_RATING);
    }
    
    /**
     * Updates doctor's rating based on patient feedback.
     * 
     * @param doctorId the doctor ID
     * @param newRating the new rating (1-5)
     * @param totalRatings the total number of ratings received
     * @throws InvalidDataException if doctor not found or invalid rating
     */
    public void updateDoctorRating(String doctorId, double newRating, int totalRatings) 
            throws InvalidDataException {
        
        if (newRating < 1.0 || newRating > 5.0) {
            throw new InvalidDataException("Rating must be between 1.0 and 5.0");
        }
        
        Doctor doctor = doctorStore.get(doctorId);
        if (doctor == null) {
            throw new InvalidDataException("Doctor with ID '" + doctorId + "' not found");
        }
        
        doctor.updateRating(newRating, totalRatings);
    }
    
    /**
     * Gets doctor statistics.
     * 
     * @return formatted statistics string
     */
    public String getDoctorStatistics() {
        List<Doctor> allDoctors = getAllDoctors();
        
        if (allDoctors.isEmpty()) {
            return "No doctors registered in the system.";
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("Doctor Statistics:\n");
        stats.append("=".repeat(40)).append("\n");
        stats.append("Total Doctors: ").append(allDoctors.size()).append("\n");
        
        // Count by specialization
        Map<Specialization, Long> specCounts = allDoctors.stream()
                .filter(d -> d.getSpecialization() != null)
                .collect(Collectors.groupingBy(Doctor::getSpecialization, Collectors.counting()));
        
        stats.append("\nBy Specialization:\n");
        specCounts.entrySet().stream()
                .sorted(Map.Entry.<Specialization, Long>comparingByValue().reversed())
                .forEach(entry -> stats.append("  ")
                        .append(entry.getKey().getDisplayName())
                        .append(": ").append(entry.getValue()).append("\n"));
        
        // Experience statistics
        double avgExperience = allDoctors.stream()
                .mapToInt(Doctor::getYearsOfExperience)
                .average()
                .orElse(0.0);
        
        long seniorDoctors = allDoctors.stream()
                .filter(Doctor::isSeniorDoctor)
                .count();
        
        stats.append("\nExperience:\n");
        stats.append("  Average Experience: ").append(String.format("%.1f years", avgExperience)).append("\n");
        stats.append("  Senior Doctors (10+ years): ").append(seniorDoctors).append("\n");
        
        // Rating statistics
        double avgRating = allDoctors.stream()
                .mapToDouble(Doctor::getRating)
                .average()
                .orElse(0.0);
        
        long highRatedDoctors = allDoctors.stream()
                .filter(d -> d.getRating() >= 4.0)
                .count();
        
        stats.append("\nRatings:\n");
        stats.append("  Average Rating: ").append(String.format("%.1f/5.0", avgRating)).append("\n");
        stats.append("  High Rated (4.0+): ").append(highRatedDoctors).append("\n");
        
        // Availability
        long availableDoctors = allDoctors.stream()
                .filter(Doctor::isAvailable)
                .count();
        
        stats.append("\nAvailability:\n");
        stats.append("  Available Doctors: ").append(availableDoctors).append("\n");
        stats.append("  Unavailable Doctors: ").append(allDoctors.size() - availableDoctors).append("\n");
        
        return stats.toString();
    }
    
    // Utility Methods
    
    /**
     * Checks if a license number is already in use.
     * 
     * @param licenseNumber the license number to check
     * @return true if duplicate exists
     */
    private boolean isDuplicateLicenseNumber(String licenseNumber) {
        return doctorStore.anyMatch(doctor -> 
                doctor.isActive() && 
                licenseNumber.equals(doctor.getLicenseNumber()));
    }
    
    /**
     * Gets the count of active doctors.
     * 
     * @return count of active doctors
     */
    public int getActiveDoctorCount() {
        return (int) doctorStore.count(Doctor::isActive);
    }
    
    /**
     * Gets the data store for external operations.
     * 
     * @return the data store
     */
    public DataStore<Doctor> getDataStore() {
        return doctorStore;
    }
    
    /**
     * Validates all doctors in the system.
     * 
     * @return validation result
     */
    public DataStore.ValidationResult validateAllDoctors() {
        DataStore.ValidationResult result = new DataStore.ValidationResult();
        
        for (Doctor doctor : doctorStore.getAll()) {
            if (!doctor.isValid()) {
                String[] errors = doctor.getValidationErrors();
                for (String error : errors) {
                    result.addError("Doctor " + doctor.getId() + ": " + error);
                }
            }
        }
        
        result.setEntityCount(doctorStore.size());
        return result;
    }
    
    /**
     * Initializes the service with sample data for demonstration.
     */
    private void initializeSampleData() {
        try {
            // Sample doctors for demonstration
            createDoctor("John", "Smith", LocalDate.of(1980, 5, 15), "Male", 
                        "john.smith@hospital.com", "+91-9876543210", "123 Medical St, City",
                        "MD123456", Specialization.CARDIOLOGY, 15, "MD, Cardiology");
            
            createDoctor("Sarah", "Johnson", LocalDate.of(1985, 8, 22), "Female", 
                        "sarah.johnson@hospital.com", "+91-9876543211", "456 Health Ave, City",
                        "MD123457", Specialization.PEDIATRICS, 10, "MD, Pediatrics");
            
            createDoctor("Michael", "Brown", LocalDate.of(1975, 3, 10), "Male", 
                        "michael.brown@hospital.com", "+91-9876543212", "789 Care Rd, City",
                        "MD123458", Specialization.ORTHOPEDICS, 20, "MS, Orthopedics");
            
        } catch (InvalidDataException e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
        }
    }
}