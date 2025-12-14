package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Patient;
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
 * Service class for managing Patient entities and operations.
 * This demonstrates business logic layer, CRUD operations, and patient-specific functionality.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class PatientService {
    
    private final DataStore<Patient> patientStore;
    private final IdGenerator idGenerator;
    
    // Predefined comparators for different sorting needs
    public static final Comparator<Patient> BY_NAME = 
            Comparator.comparing(Patient::getLastName)
                     .thenComparing(Patient::getFirstName);
    
    public static final Comparator<Patient> BY_AGE = 
            Comparator.comparing(Patient::getAge);
    
    public static final Comparator<Patient> BY_REGISTRATION_DATE = 
            Comparator.comparing(Patient::getRegistrationDate);
    
    public static final Comparator<Patient> BY_VISIT_COUNT = 
            Comparator.comparing(Patient::getVisitCount).reversed();
    
    public static final Comparator<Patient> BY_PATIENT_TYPE = 
            Comparator.comparing(Patient::getPatientType);
    
    public static final Comparator<Patient> BY_PRIORITY = 
            Comparator.comparing(Patient::getPriority);
    
    /**
     * Constructor that initializes the service with a data store.
     */
    public PatientService() {
        this.patientStore = new DataStore<>("Patient");
        this.idGenerator = IdGenerator.getInstance();
        
        // Set default comparator for the store
        this.patientStore.setDefaultComparator(BY_NAME);
        
        // Initialize with some sample data for demonstration
        initializeSampleData();
    }
    
    /**
     * Constructor with external data store (for dependency injection).
     * 
     * @param patientStore the data store to use
     */
    public PatientService(DataStore<Patient> patientStore) {
        this.patientStore = patientStore;
        this.idGenerator = IdGenerator.getInstance();
        this.patientStore.setDefaultComparator(BY_NAME);
    }
    
    // CRUD Operations
    
    /**
     * Creates a new patient.
     * 
     * @param firstName the first name
     * @param lastName the last name
     * @param dateOfBirth the date of birth
     * @param gender the gender
     * @param email the email address
     * @param phone the phone number
     * @param address the address
     * @param patientType the patient type (INPATIENT, OUTPATIENT, EMERGENCY)
     * @param insuranceProvider the insurance provider (optional)
     * @return the created patient
     * @throws InvalidDataException if validation fails
     */
    public Patient createPatient(String firstName, String lastName, LocalDate dateOfBirth,
                                String gender, String email, String phone, String address,
                                String patientType, String insuranceProvider) throws InvalidDataException {
        
        // Validate input data
        List<String> errors = Validator.validatePerson(firstName, lastName, dateOfBirth, gender, email, phone);
        
        // Additional patient-specific validations
        try {
            Validator.validatePatientType(patientType);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        if (!errors.isEmpty()) {
            throw new InvalidDataException("Patient validation failed: " + String.join(", ", errors));
        }
        
        // Generate new ID
        String patientId = idGenerator.generatePatientId();
        
        // Create patient entity
        Patient patient = new Patient(patientId, firstName, lastName, dateOfBirth, gender,
                                    email, phone, address, patientType, insuranceProvider);
        
        // Store the patient
        patientStore.store(patientId, patient);
        
        return patient;
    }
    
    /**
     * Retrieves a patient by ID.
     * 
     * @param patientId the patient ID
     * @return the patient, or null if not found
     */
    public Patient getPatientById(String patientId) {
        return patientStore.get(patientId);
    }
    
    /**
     * Updates an existing patient.
     * 
     * @param patientId the patient ID
     * @param updatedPatient the updated patient data
     * @return the updated patient
     * @throws InvalidDataException if validation fails or patient not found
     */
    public Patient updatePatient(String patientId, Patient updatedPatient) throws InvalidDataException {
        Patient existingPatient = patientStore.get(patientId);
        if (existingPatient == null) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found");
        }
        
        // Validate the updated patient
        updatedPatient.validateAndThrow();
        
        // Ensure ID remains the same
        updatedPatient.setId(patientId);
        
        // Preserve certain fields that shouldn't change
        updatedPatient.setRegistrationDate(existingPatient.getRegistrationDate());
        
        // Update in store
        patientStore.update(patientId, updatedPatient);
        
        return updatedPatient;
    }
    
    /**
     * Deletes a patient by ID.
     * 
     * @param patientId the patient ID
     * @return true if deleted, false if not found
     */
    public boolean deletePatient(String patientId) {
        Patient removed = patientStore.remove(patientId);
        return removed != null;
    }
    
    /**
     * Soft deletes a patient (marks as inactive).
     * 
     * @param patientId the patient ID
     * @return true if deactivated, false if not found
     */
    public boolean deactivatePatient(String patientId) {
        Patient patient = patientStore.get(patientId);
        if (patient != null) {
            patient.setActive(false);
            return true;
        }
        return false;
    }
    
    // Query Operations
    
    /**
     * Gets all patients.
     * 
     * @return list of all active patients
     */
    public List<Patient> getAllPatients() {
        return patientStore.getAll().stream()
                .filter(Patient::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all patients sorted by name.
     * 
     * @return sorted list of patients
     */
    public List<Patient> getAllPatientsSorted() {
        return patientStore.getAllSorted(BY_NAME).stream()
                .filter(Patient::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets patients sorted by a specific criteria.
     * 
     * @param comparator the comparator to use for sorting
     * @return sorted list of patients
     */
    public List<Patient> getAllPatientsSorted(Comparator<Patient> comparator) {
        return patientStore.getAllSorted(comparator).stream()
                .filter(Patient::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds patients by type.
     * 
     * @param patientType the patient type to search for
     * @return list of matching patients
     */
    public List<Patient> findPatientsByType(String patientType) {
        return patientStore.findWhere(patient -> 
                patient.isActive() && 
                patientType.equalsIgnoreCase(patient.getPatientType()));
    }
    
    /**
     * Finds patients by age range.
     * 
     * @param minAge minimum age
     * @param maxAge maximum age
     * @return list of matching patients
     */
    public List<Patient> findPatientsByAgeRange(int minAge, int maxAge) {
        return patientStore.findWhere(patient -> 
                patient.isActive() &&
                patient.getAge() >= minAge && 
                patient.getAge() <= maxAge);
    }
    
    /**
     * Finds patients with insurance.
     * 
     * @return list of patients with insurance
     */
    public List<Patient> findPatientsWithInsurance() {
        return patientStore.findWhere(patient -> 
                patient.isActive() && patient.hasInsurance());
    }
    
    /**
     * Finds patients without insurance.
     * 
     * @return list of patients without insurance
     */
    public List<Patient> findPatientsWithoutInsurance() {
        return patientStore.findWhere(patient -> 
                patient.isActive() && !patient.hasInsurance());
    }
    
    /**
     * Finds senior citizen patients.
     * 
     * @return list of senior citizen patients
     */
    public List<Patient> findSeniorCitizenPatients() {
        return patientStore.findWhere(patient -> 
                patient.isActive() && patient.isSeniorCitizen());
    }
    
    /**
     * Finds minor patients.
     * 
     * @return list of minor patients
     */
    public List<Patient> findMinorPatients() {
        return patientStore.findWhere(patient -> 
                patient.isActive() && patient.isMinor());
    }
    
    /**
     * Finds regular patients (with multiple visits).
     * 
     * @return list of regular patients
     */
    public List<Patient> findRegularPatients() {
        return patientStore.findWhere(patient -> 
                patient.isActive() && patient.isRegularPatient());
    }
    
    /**
     * Finds patients registered within a date range.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of patients registered within the range
     */
    public List<Patient> findPatientsRegisteredBetween(LocalDate startDate, LocalDate endDate) {
        return patientStore.findWhere(patient -> 
                patient.isActive() &&
                patient.getRegistrationDate() != null &&
                !patient.getRegistrationDate().isBefore(startDate) &&
                !patient.getRegistrationDate().isAfter(endDate));
    }
    
    /**
     * Finds patients with specific allergies.
     * 
     * @param allergen the allergen to search for
     * @return list of patients allergic to the specified substance
     */
    public List<Patient> findPatientsWithAllergy(String allergen) {
        return patientStore.findWhere(patient -> 
                patient.isActive() && patient.isAllergicTo(allergen));
    }
    
    /**
     * Finds patients currently on specific medication.
     * 
     * @param medication the medication to search for
     * @return list of patients currently taking the medication
     */
    public List<Patient> findPatientsOnMedication(String medication) {
        return patientStore.findWhere(patient -> 
                patient.isActive() && patient.isCurrentlyTaking(medication));
    }
    
    // Search Operations
    
    /**
     * Searches patients using various criteria.
     * 
     * @param searchTerm the search term
     * @return list of matching patients sorted by relevance
     */
    public List<Patient> searchPatients(String searchTerm) {
        return patientStore.searchAndSort(searchTerm).stream()
                .filter(Patient::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Advanced search with multiple criteria.
     * 
     * @param name patient name (partial match)
     * @param patientType patient type filter (optional)
     * @param minAge minimum age (optional)
     * @param maxAge maximum age (optional)
     * @param hasInsurance insurance filter (optional)
     * @param bloodGroup blood group filter (optional)
     * @return list of matching patients
     */
    public List<Patient> advancedSearch(String name, String patientType, 
                                      Integer minAge, Integer maxAge, 
                                      Boolean hasInsurance, String bloodGroup) {
        
        Predicate<Patient> criteria = patient -> patient.isActive();
        
        // Add name filter
        if (name != null && !name.trim().isEmpty()) {
            String searchName = name.toLowerCase().trim();
            criteria = criteria.and(patient -> 
                    patient.getFullName().toLowerCase().contains(searchName));
        }
        
        // Add patient type filter
        if (patientType != null && !patientType.trim().isEmpty()) {
            criteria = criteria.and(patient -> 
                    patientType.equalsIgnoreCase(patient.getPatientType()));
        }
        
        // Add age filters
        if (minAge != null) {
            criteria = criteria.and(patient -> patient.getAge() >= minAge);
        }
        if (maxAge != null) {
            criteria = criteria.and(patient -> patient.getAge() <= maxAge);
        }
        
        // Add insurance filter
        if (hasInsurance != null) {
            criteria = criteria.and(patient -> patient.hasInsurance() == hasInsurance);
        }
        
        // Add blood group filter
        if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
            criteria = criteria.and(patient -> 
                    bloodGroup.equalsIgnoreCase(patient.getBloodGroup()));
        }
        
        return patientStore.findWhere(criteria);
    }
    
    // Business Logic Methods
    
    /**
     * Records a patient visit.
     * 
     * @param patientId the patient ID
     * @throws InvalidDataException if patient not found
     */
    public void recordVisit(String patientId) throws InvalidDataException {
        Patient patient = patientStore.get(patientId);
        if (patient == null) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found");
        }
        
        patient.incrementVisitCount();
    }
    
    /**
     * Adds medical history to a patient.
     * 
     * @param patientId the patient ID
     * @param historyItem the medical history item to add
     * @throws InvalidDataException if patient not found
     */
    public void addMedicalHistory(String patientId, String historyItem) throws InvalidDataException {
        Patient patient = patientStore.get(patientId);
        if (patient == null) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found");
        }
        
        patient.addMedicalHistory(historyItem);
    }
    
    /**
     * Adds an allergy to a patient.
     * 
     * @param patientId the patient ID
     * @param allergy the allergy to add
     * @throws InvalidDataException if patient not found
     */
    public void addAllergy(String patientId, String allergy) throws InvalidDataException {
        Patient patient = patientStore.get(patientId);
        if (patient == null) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found");
        }
        
        patient.addAllergy(allergy);
    }
    
    /**
     * Adds a current medication to a patient.
     * 
     * @param patientId the patient ID
     * @param medication the medication to add
     * @throws InvalidDataException if patient not found
     */
    public void addCurrentMedication(String patientId, String medication) throws InvalidDataException {
        Patient patient = patientStore.get(patientId);
        if (patient == null) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found");
        }
        
        patient.addCurrentMedication(medication);
    }
    
    /**
     * Removes a medication from a patient's current medications.
     * 
     * @param patientId the patient ID
     * @param medication the medication to remove
     * @throws InvalidDataException if patient not found
     */
    public void removeMedication(String patientId, String medication) throws InvalidDataException {
        Patient patient = patientStore.get(patientId);
        if (patient == null) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found");
        }
        
        patient.removeMedication(medication);
    }
    
    /**
     * Gets patient statistics.
     * 
     * @return formatted statistics string
     */
    public String getPatientStatistics() {
        List<Patient> allPatients = getAllPatients();
        
        if (allPatients.isEmpty()) {
            return "No patients registered in the system.";
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("Patient Statistics:\n");
        stats.append("=".repeat(40)).append("\n");
        stats.append("Total Patients: ").append(allPatients.size()).append("\n");
        
        // Count by type
        Map<String, Long> typeCounts = allPatients.stream()
                .collect(Collectors.groupingBy(Patient::getPatientType, Collectors.counting()));
        
        stats.append("\nBy Type:\n");
        typeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> stats.append("  ")
                        .append(entry.getKey())
                        .append(": ").append(entry.getValue()).append("\n"));
        
        // Age demographics
        double avgAge = allPatients.stream()
                .mapToInt(Patient::getAge)
                .average()
                .orElse(0.0);
        
        long children = allPatients.stream().filter(Patient::isMinor).count();
        long seniors = allPatients.stream().filter(Patient::isSeniorCitizen).count();
        long adults = allPatients.size() - children - seniors;
        
        stats.append("\nAge Demographics:\n");
        stats.append("  Average Age: ").append(String.format("%.1f years", avgAge)).append("\n");
        stats.append("  Children (< 18): ").append(children).append("\n");
        stats.append("  Adults (18-65): ").append(adults).append("\n");
        stats.append("  Seniors (65+): ").append(seniors).append("\n");
        
        // Insurance statistics
        long withInsurance = allPatients.stream().filter(Patient::hasInsurance).count();
        long withoutInsurance = allPatients.size() - withInsurance;
        
        stats.append("\nInsurance Coverage:\n");
        stats.append("  With Insurance: ").append(withInsurance).append(" (")
             .append(String.format("%.1f%%", (double)withInsurance/allPatients.size()*100)).append(")\n");
        stats.append("  Without Insurance: ").append(withoutInsurance).append(" (")
             .append(String.format("%.1f%%", (double)withoutInsurance/allPatients.size()*100)).append(")\n");
        
        // Visit statistics
        long regularPatients = allPatients.stream().filter(Patient::isRegularPatient).count();
        double avgVisits = allPatients.stream()
                .mapToInt(Patient::getVisitCount)
                .average()
                .orElse(0.0);
        
        stats.append("\nVisit Statistics:\n");
        stats.append("  Regular Patients (3+ visits): ").append(regularPatients).append("\n");
        stats.append("  Average Visits: ").append(String.format("%.1f", avgVisits)).append("\n");
        
        // Medical conditions
        long patientsWithAllergies = allPatients.stream()
                .filter(Patient::hasAllergies)
                .count();
        
        long patientsOnMedication = allPatients.stream()
                .filter(p -> !p.getCurrentMedications().isEmpty())
                .count();
        
        stats.append("\nMedical Information:\n");
        stats.append("  Patients with Allergies: ").append(patientsWithAllergies).append("\n");
        stats.append("  Patients on Medication: ").append(patientsOnMedication).append("\n");
        
        return stats.toString();
    }
    
    /**
     * Gets patients requiring priority attention.
     * 
     * @return list of high priority patients
     */
    public List<Patient> getHighPriorityPatients() {
        return patientStore.findWhere(patient -> 
                patient.isActive() && "HIGH".equals(patient.getPriority()));
    }
    
    // Utility Methods
    
    /**
     * Gets the count of active patients.
     * 
     * @return count of active patients
     */
    public int getActivePatientCount() {
        return (int) patientStore.count(Patient::isActive);
    }
    
    /**
     * Gets the data store for external operations.
     * 
     * @return the data store
     */
    public DataStore<Patient> getDataStore() {
        return patientStore;
    }
    
    /**
     * Validates all patients in the system.
     * 
     * @return validation result
     */
    public DataStore.ValidationResult validateAllPatients() {
        DataStore.ValidationResult result = new DataStore.ValidationResult();
        
        for (Patient patient : patientStore.getAll()) {
            if (!patient.isValid()) {
                String[] errors = patient.getValidationErrors();
                for (String error : errors) {
                    result.addError("Patient " + patient.getId() + ": " + error);
                }
            }
        }
        
        result.setEntityCount(patientStore.size());
        return result;
    }
    
    /**
     * Initializes the service with sample data for demonstration.
     */
    private void initializeSampleData() {
        try {
            // Sample patients for demonstration
            createPatient("Alice", "Johnson", LocalDate.of(1990, 3, 15), "Female", 
                         "alice.johnson@email.com", "+91-9876543220", "123 Main St, City",
                         "OUTPATIENT", "Health Plus Insurance");
            
            createPatient("Bob", "Smith", LocalDate.of(1985, 7, 22), "Male", 
                         "bob.smith@email.com", "+91-9876543221", "456 Oak Ave, City",
                         "OUTPATIENT", null);
            
            createPatient("Carol", "Davis", LocalDate.of(1955, 12, 10), "Female", 
                         "carol.davis@email.com", "+91-9876543222", "789 Pine Rd, City",
                         "INPATIENT", "Senior Care Insurance");
            
        } catch (InvalidDataException e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
        }
    }
}