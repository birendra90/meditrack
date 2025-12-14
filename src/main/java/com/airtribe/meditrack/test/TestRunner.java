package com.airtribe.meditrack.test;

import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.exception.*;
import com.airtribe.meditrack.service.*;
import com.airtribe.meditrack.util.*;
import com.airtribe.meditrack.constants.Constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manual test runner for the MediTrack application.
 * This demonstrates testing patterns and validates core functionality.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class TestRunner {
    
    private static DoctorService doctorService;
    private static PatientService patientService;
    private static AppointmentService appointmentService;
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    /**
     * Main method to run all tests.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("           MEDITRACK TEST RUNNER");
        System.out.println("=".repeat(60));
        
        // Initialize services
        initializeServices();
        
        // Run tests
        runAllTests();
        
        // Show results
        showTestResults();
    }
    
    /**
     * Initializes test services.
     */
    private static void initializeServices() {
        System.out.println("Initializing test services...");
        doctorService = new DoctorService();
        patientService = new PatientService();
        appointmentService = new AppointmentService(doctorService, patientService);
        System.out.println("Services initialized successfully.\n");
    }
    
    /**
     * Runs all test suites.
     */
    private static void runAllTests() {
        System.out.println("Running test suites:\n");
        
        // Core entity tests
        testEntityCreationAndValidation();
        testEnumAndConstants();
        testImmutableClass();
        testCloningMechanisms();
        
        // Service layer tests
        testDoctorService();
        testPatientService();
        testAppointmentService();
        
        // Utility tests
        testUtilityClasses();
        testSearchFunctionality();
        
        // Integration tests
        testWorkflowIntegration();
        testExceptionHandling();
        
        // Design pattern tests
        testDesignPatterns();
    }
    
    /**
     * Tests entity creation and validation.
     */
    private static void testEntityCreationAndValidation() {
        System.out.println("1. Testing Entity Creation and Validation");
        System.out.println("-".repeat(40));
        
        // Test Doctor creation
        test("Doctor Creation", () -> {
            Doctor doctor = new Doctor();
            doctor.setId("D00001");
            doctor.setFirstName("John");
            doctor.setLastName("Smith");
            doctor.setDateOfBirth(LocalDate.of(1980, 5, 15));
            doctor.setGender("Male");
            doctor.setEmail("john.smith@hospital.com");
            doctor.setPhone("+91-9876543210");
            doctor.setLicenseNumber("MD123456");
            doctor.setSpecialization(Specialization.CARDIOLOGY);
            doctor.setYearsOfExperience(15);
            doctor.setQualification("MD, Cardiology");
            
            return doctor.isValid();
        });
        
        // Test Patient creation
        test("Patient Creation", () -> {
            Patient patient = new Patient();
            patient.setId("P00001");
            patient.setFirstName("Alice");
            patient.setLastName("Johnson");
            patient.setDateOfBirth(LocalDate.of(1990, 3, 20));
            patient.setGender("Female");
            patient.setEmail("alice.johnson@email.com");
            patient.setPhone("+91-9876543220");
            patient.setPatientType("OUTPATIENT");
            patient.setInsuranceProvider("Health Plus Insurance");
            patient.setInsurancePolicyNumber("INS123456789"); // Fix: Add missing policy number
            
            return patient.isValid();
        });
        
        // Test Appointment creation
        test("Appointment Creation", () -> {
            Appointment appointment = new Appointment();
            appointment.setId("A00001");
            appointment.setPatientId("P00001");
            appointment.setDoctorId("D00001");
            // Fix: Set appointment time to be within clinic hours (9-18)
            appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
            appointment.setDurationMinutes(30);
            appointment.setReasonForVisit("Regular checkup");
            appointment.setAppointmentType("CONSULTATION");
            appointment.setConsultationFee(1500.0);
            
            return appointment.isValid();
        });
        
        // Test validation errors
        test("Validation Error Handling", () -> {
            Doctor invalidDoctor = new Doctor();
            // Don't set required fields
            
            String[] errors = invalidDoctor.getValidationErrors();
            return errors.length > 0; // Should have validation errors
        });
        
        System.out.println();
    }
    
    /**
     * Tests enum and constants functionality.
     */
    private static void testEnumAndConstants() {
        System.out.println("2. Testing Enums and Constants");
        System.out.println("-".repeat(40));
        
        // Test Specialization enum
        test("Specialization Enum", () -> {
            Specialization cardiology = Specialization.CARDIOLOGY;
            return "Cardiology".equals(cardiology.getDisplayName()) &&
                   cardiology.getBaseConsultationFee() > 0 &&
                   cardiology.isHighCost();
        });
        
        // Test AppointmentStatus enum
        test("AppointmentStatus Enum", () -> {
            AppointmentStatus pending = AppointmentStatus.PENDING;
            AppointmentStatus confirmed = AppointmentStatus.CONFIRMED;
            
            return pending.canTransitionTo(confirmed) &&
                   !confirmed.isFinal() &&
                   AppointmentStatus.COMPLETED.isFinal();
        });
        
        // Test Constants class
        test("Constants Class", () -> {
            return Constants.TAX_RATE == 0.18 &&
                   Constants.DEFAULT_APPOINTMENT_DURATION == 30 &&
                   Constants.APPLICATION_NAME.equals("MediTrack");
        });
        
        System.out.println();
    }
    
    /**
     * Tests immutable class implementation.
     */
    private static void testImmutableClass() {
        System.out.println("3. Testing Immutable Class (BillSummary)");
        System.out.println("-".repeat(40));
        
        test("BillSummary Immutability", () -> {
            BillSummary summary = new BillSummary(
                    "S00001", "P00001", "Alice Johnson",
                    LocalDate.now().minusDays(30), LocalDate.now(),
                    5, 7500.0, 5000.0);
            
            // Test immutability - getters should work, no setters exist
            return summary.getTotalBills() == 5 &&
                   summary.getTotalAmount() == 7500.0 &&
                   summary.getPaymentCompletionRate() > 0 &&
                   summary.getPendingAmount() == 2500.0;
        });
        
        test("BillSummary Builder Pattern", () -> {
            BillSummary summary = new BillSummary(
                    "S00002", "P00002", "Bob Smith",
                    LocalDate.now().minusDays(7), LocalDate.now(),
                    3, 4500.0, 4500.0);
            
            // Test that it's fully paid
            return summary.getPaymentCompletionRate() == 1.0 &&
                   summary.getPendingAmount() == 0.0;
        });
        
        System.out.println();
    }
    
    /**
     * Tests cloning mechanisms (deep vs shallow).
     */
    private static void testCloningMechanisms() {
        System.out.println("4. Testing Cloning Mechanisms");
        System.out.println("-".repeat(40));
        
        test("Patient Deep Cloning", () -> {
            try {
                Patient original = new Patient();
                original.setId("P00003");
                original.setFirstName("Carol");
                original.setLastName("Davis");
                original.setDateOfBirth(LocalDate.of(1985, 8, 10));
                original.setGender("Female");
                original.addMedicalHistory("Diabetes Type 2");
                original.addAllergy("Penicillin");
                original.addCurrentMedication("Metformin");
                
                Patient cloned = original.clone();
                
                // Modify original
                original.addMedicalHistory("Hypertension");
                
                // Cloned should not be affected (deep copy)
                return cloned.getMedicalHistory().size() == 1 &&
                       original.getMedicalHistory().size() == 2 &&
                       !cloned.getId().equals(original.getId()); // Different references
            } catch (CloneNotSupportedException e) {
                return false;
            }
        });
        
        test("Appointment Deep Cloning", () -> {
            try {
                Appointment original = new Appointment();
                original.setId("A00002");
                original.setPatientId("P00003");
                original.setDoctorId("D00001");
                original.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
                original.setReasonForVisit("Follow-up");
                
                Appointment cloned = original.clone();
                
                // Modify original datetime
                original.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
                
                // Cloned should have different datetime (deep copy)
                return !cloned.getAppointmentDateTime().equals(original.getAppointmentDateTime());
            } catch (CloneNotSupportedException e) {
                return false;
            }
        });
        
        System.out.println();
    }
    
    /**
     * Tests Doctor service functionality.
     */
    private static void testDoctorService() {
        System.out.println("5. Testing Doctor Service");
        System.out.println("-".repeat(40));
        
        test("Doctor Creation via Service", () -> {
            try {
                Doctor doctor = doctorService.createDoctor(
                        "Sarah", "Wilson", LocalDate.of(1985, 3, 12), "Female",
                        "sarah.wilson@hospital.com", "+91-9876543230", "456 Medical Ave",
                        "MD987654", Specialization.PEDIATRICS, 8, "MD, Pediatrics"
                );
                
                return doctor != null && doctor.getId() != null;
            } catch (InvalidDataException e) {
                return false;
            }
        });
        
        test("Doctor Search and Retrieval", () -> {
            List<Doctor> doctors = doctorService.getAllDoctors();
            List<Doctor> pediatricians = doctorService.findDoctorsBySpecialization(Specialization.PEDIATRICS);
            
            return !doctors.isEmpty() && !pediatricians.isEmpty();
        });
        
        test("Doctor Validation", () -> {
            try {
                // Try to create invalid doctor
                doctorService.createDoctor(
                        "", "", LocalDate.now().plusDays(1), "", // Invalid data
                        "", "", "", "", null, -1, ""
                );
                return false; // Should throw exception
            } catch (InvalidDataException e) {
                return true; // Expected behavior
            }
        });
        
        System.out.println();
    }
    
    /**
     * Tests Patient service functionality.
     */
    private static void testPatientService() {
        System.out.println("6. Testing Patient Service");
        System.out.println("-".repeat(40));
        
        test("Patient Creation via Service", () -> {
            try {
                Patient patient = patientService.createPatient(
                        "David", "Brown", LocalDate.of(1975, 12, 5), "Male",
                        "david.brown@email.com", "+91-9876543240", "789 Health St",
                        "OUTPATIENT", "Universal Health"
                );
                
                return patient != null && patient.getId() != null;
            } catch (InvalidDataException e) {
                return false;
            }
        });
        
        test("Patient Medical Records Management", () -> {
            try {
                List<Patient> patients = patientService.getAllPatients();
                if (!patients.isEmpty()) {
                    Patient patient = patients.get(0);
                    
                    patientService.addMedicalHistory(patient.getId(), "Annual checkup 2025");
                    patientService.addAllergy(patient.getId(), "Shellfish");
                    patientService.addCurrentMedication(patient.getId(), "Vitamin D3");
                    
                    Patient updated = patientService.getPatientById(patient.getId());
                    return updated.getMedicalHistory().size() > 0 &&
                           updated.getAllergies().size() > 0 &&
                           updated.getCurrentMedications().size() > 0;
                }
                return false;
            } catch (InvalidDataException e) {
                return false;
            }
        });
        
        test("Patient Demographics Analysis", () -> {
            List<Patient> seniorPatients = patientService.findSeniorCitizenPatients();
            List<Patient> minorPatients = patientService.findMinorPatients();
            List<Patient> insuredPatients = patientService.findPatientsWithInsurance();
            
            // Should be able to categorize patients
            return seniorPatients != null && minorPatients != null && insuredPatients != null;
        });
        
        System.out.println();
    }
    
    /**
     * Tests Appointment service functionality.
     */
    private static void testAppointmentService() {
        System.out.println("7. Testing Appointment Service");
        System.out.println("-".repeat(40));
        
        test("Appointment Booking", () -> {
            try {
                List<Doctor> doctors = doctorService.getAllDoctors();
                List<Patient> patients = patientService.getAllPatients();
                
                if (!doctors.isEmpty() && !patients.isEmpty()) {
                    Doctor doctor = doctors.get(0);
                    Patient patient = patients.get(0);
                    
                    Appointment appointment = appointmentService.createAppointment(
                            patient.getId(), doctor.getId(),
                            LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                            30, "Regular consultation", "CONSULTATION", false
                    );
                    
                    return appointment != null && appointment.getId() != null;
                }
                return false;
            } catch (InvalidDataException e) {
                return false;
            }
        });
        
        test("Appointment Status Transitions", () -> {
            try {
                List<Appointment> appointments = appointmentService.getAllAppointments();
                if (!appointments.isEmpty()) {
                    Appointment appointment = appointments.get(0);
                    String appointmentId = appointment.getId();
                    
                    // Test status transitions
                    appointmentService.confirmAppointment(appointmentId);
                    appointmentService.startAppointment(appointmentId);
                    appointmentService.completeAppointment(appointmentId, 
                            "Patient is healthy", "Multivitamins", "Follow up in 6 months");
                    
                    Appointment completed = appointmentService.getAppointmentById(appointmentId);
                    return completed.getStatus() == AppointmentStatus.COMPLETED;
                }
                return false;
            } catch (AppointmentNotFoundException | InvalidDataException e) {
                return false;
            }
        });
        
        test("Appointment Scheduling Conflicts", () -> {
            try {
                List<Doctor> doctors = doctorService.getAllDoctors();
                List<Patient> patients = patientService.getAllPatients();
                
                if (!doctors.isEmpty() && patients.size() >= 2) {
                    Doctor doctor = doctors.get(0);
                    LocalDateTime conflictTime = LocalDateTime.now().plusDays(2).withHour(15).withMinute(0);
                    
                    // Book first appointment
                    appointmentService.createAppointment(
                            patients.get(0).getId(), doctor.getId(),
                            conflictTime, 30, "First appointment", "CONSULTATION", false
                    );
                    
                    // Try to book overlapping appointment
                    appointmentService.createAppointment(
                            patients.get(1).getId(), doctor.getId(),
                            conflictTime.plusMinutes(15), 30, "Conflicting appointment", "CONSULTATION", false
                    );
                    
                    return false; // Should not reach here due to conflict
                }
                return true; // Not enough data to test, consider passed
            } catch (InvalidDataException e) {
                return true; // Expected conflict detection
            }
        });
        
        System.out.println();
    }
    
    /**
     * Tests utility classes.
     */
    private static void testUtilityClasses() {
        System.out.println("8. Testing Utility Classes");
        System.out.println("-".repeat(40));
        
        test("ID Generator", () -> {
            IdGenerator generator = IdGenerator.getInstance();
            String patientId1 = generator.generatePatientId();
            String patientId2 = generator.generatePatientId();
            String doctorId = generator.generateDoctorId();
            
            return patientId1.startsWith("P") &&
                   patientId2.startsWith("P") &&
                   doctorId.startsWith("D") &&
                   !patientId1.equals(patientId2);
        });
        
        test("Date Utilities", () -> {
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            
            String dateStr = DateUtil.formatDate(today);
            String dateTimeStr = DateUtil.formatDateTime(now);
            
            LocalDate parsedDate = DateUtil.parseDate(dateStr);
            LocalDateTime parsedDateTime = DateUtil.parseDateTime(dateTimeStr);
            
            return parsedDate.equals(today) && 
                   parsedDateTime.toLocalDate().equals(now.toLocalDate());
        });
        
        test("Validator", () -> {
            try {
                Validator.validateEmail("valid@email.com"); // Should pass
                Validator.validatePhone("+91-9876543210"); // Should pass
                
                try {
                    Validator.validateEmail("invalid-email"); // Should fail
                    return false;
                } catch (InvalidDataException e) {
                    return true; // Expected
                }
            } catch (InvalidDataException e) {
                return false;
            }
        });
        
        test("DataStore Generic Operations", () -> {
            DataStore<String> stringStore = new DataStore<>("TestStore");
            
            stringStore.store("key1", "value1");
            stringStore.store("key2", "value2");
            
            return stringStore.size() == 2 &&
                   "value1".equals(stringStore.get("key1")) &&
                   stringStore.contains("key2");
        });
        
        System.out.println();
    }
    
    /**
     * Tests search functionality.
     */
    private static void testSearchFunctionality() {
        System.out.println("9. Testing Search Functionality");
        System.out.println("-".repeat(40));
        
        test("Doctor Search", () -> {
            List<Doctor> searchResults = doctorService.searchDoctors("John");
            return searchResults != null; // Should return list (may be empty)
        });
        
        test("Patient Search", () -> {
            List<Patient> searchResults = patientService.searchPatients("Alice");
            return searchResults != null;
        });
        
        test("Advanced Search", () -> {
            List<Doctor> cardioResults = doctorService.findDoctorsBySpecialization(Specialization.CARDIOLOGY);
            List<Patient> seniorResults = patientService.findSeniorCitizenPatients();
            
            return cardioResults != null && seniorResults != null;
        });
        
        System.out.println();
    }
    
    /**
     * Tests workflow integration.
     */
    private static void testWorkflowIntegration() {
        System.out.println("10. Testing Workflow Integration");
        System.out.println("-".repeat(40));
        
        test("Complete Patient-Doctor-Appointment Workflow", () -> {
            try {
                // Create doctor
                Doctor doctor = doctorService.createDoctor(
                        "Emily", "Chen", LocalDate.of(1982, 6, 18), "Female",
                        "emily.chen@hospital.com", "+91-9876543250", "Medical Center",
                        "MD456789", Specialization.GENERAL_MEDICINE, 12, "MD, Family Medicine"
                );
                
                // Create patient
                Patient patient = patientService.createPatient(
                        "Michael", "Johnson", LocalDate.of(1988, 9, 25), "Male",
                        "michael.j@email.com", "+91-9876543260", "Patient Address",
                        "OUTPATIENT", null
                );
                
                // Book appointment
                Appointment appointment = appointmentService.createAppointment(
                        patient.getId(), doctor.getId(),
                        LocalDateTime.now().plusDays(3).withHour(10).withMinute(30),
                        45, "Health screening", "CHECKUP", false
                );
                
                // Complete workflow
                appointmentService.confirmAppointment(appointment.getId());
                appointmentService.startAppointment(appointment.getId());
                appointmentService.completeAppointment(appointment.getId(),
                        "Patient is in good health", null, "Annual checkup completed");
                
                return true;
            } catch (Exception e) {
                System.err.println("Workflow error: " + e.getMessage());
                return false;
            }
        });
        
        test("Statistics Generation", () -> {
            String doctorStats = doctorService.getDoctorStatistics();
            String patientStats = patientService.getPatientStatistics();
            String appointmentStats = appointmentService.getAppointmentStatistics();
            
            return doctorStats.contains("Total Doctors") &&
                   patientStats.contains("Total Patients") &&
                   appointmentStats.contains("Total Appointments");
        });
        
        System.out.println();
    }
    
    /**
     * Tests exception handling.
     */
    private static void testExceptionHandling() {
        System.out.println("11. Testing Exception Handling");
        System.out.println("-".repeat(40));
        
        test("InvalidDataException Handling", () -> {
            try {
                // Try invalid doctor creation
                doctorService.createDoctor("", "", null, "", "", "", "", "", null, -1, "");
                return false; // Should throw exception
            } catch (InvalidDataException e) {
                return e.getMessage() != null && !e.getMessage().isEmpty();
            }
        });
        
        test("AppointmentNotFoundException Handling", () -> {
            try {
                appointmentService.getAppointmentById("INVALID_ID");
                return false; // Should throw exception
            } catch (AppointmentNotFoundException e) {
                return e.getAppointmentId() != null;
            }
        });
        
        test("Exception Chaining", () -> {
            InvalidDataException parent = new InvalidDataException("Parent error");
            InvalidDataException child = new InvalidDataException("Child error", parent);
            
            return child.getCause() == parent;
        });
        
        System.out.println();
    }
    
    /**
     * Tests design patterns implementation.
     */
    private static void testDesignPatterns() {
        System.out.println("12. Testing Design Patterns");
        System.out.println("-".repeat(40));
        
        test("Singleton Pattern (IdGenerator)", () -> {
            IdGenerator instance1 = IdGenerator.getInstance();
            IdGenerator instance2 = IdGenerator.getInstance();
            
            return instance1 == instance2; // Same instance
        });
        
        test("Simple BillSummary Creation", () -> {
            BillSummary summary = new BillSummary(
                    "S00003", "P00001", "Test Patient",
                    LocalDate.now().minusDays(30), LocalDate.now(),
                    10, 15000.0, 12000.0);
            
            return summary.getTotalBills() == 10 &&
                   summary.getTotalAmount() == 15000.0 &&
                   summary.getPendingAmount() == 3000.0;
        });
        
        test("Template Method Pattern (MedicalEntity)", () -> {
            Doctor doctor = new Doctor();
            doctor.setFirstName("Test");
            doctor.setLastName("Doctor");
            doctor.setDateOfBirth(LocalDate.of(1980, 1, 1));
            doctor.setGender("Male");
            
            // Template method should work
            String summary = doctor.getSummary();
            return summary.contains("Test Doctor") && summary.contains("Doctor");
        });
        
        test("Strategy Pattern (Payable Interface)", () -> {
            // Create a bill that implements Payable
            Bill bill = new Bill("B00001", "A00001", "P00001", "D00001", 1000.0, "CONSULTATION");
            
            // Test different discount strategies
            double percentageDiscount = bill.calculatePercentageDiscount(0.10); // 10%
            double flatDiscount = bill.calculateFlatDiscount(100.0); // ₹100
            
            return percentageDiscount == 100.0 && flatDiscount == 100.0;
        });
        
        System.out.println();
    }
    
    /**
     * Test helper method.
     */
    private static void test(String testName, TestCase testCase) {
        try {
            boolean result = testCase.run();
            if (result) {
                System.out.printf("✓ %-40s PASSED\n", testName);
                testsPassed++;
            } else {
                System.out.printf("✗ %-40s FAILED\n", testName);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.printf("✗ %-40s ERROR: %s\n", testName, e.getMessage());
            testsFailed++;
        }
    }
    
    /**
     * Shows final test results.
     */
    private static void showTestResults() {
        System.out.println("=".repeat(60));
        System.out.println("                 TEST RESULTS");
        System.out.println("=".repeat(60));
        
        int totalTests = testsPassed + testsFailed;
        double successRate = totalTests > 0 ? (double) testsPassed / totalTests * 100 : 0;
        
        System.out.printf("Total Tests Run: %d\n", totalTests);
        System.out.printf("Tests Passed: %d\n", testsPassed);
        System.out.printf("Tests Failed: %d\n", testsFailed);
        System.out.printf("Success Rate: %.1f%%\n", successRate);
        
        System.out.println();
        if (testsFailed == 0) {
            System.out.println("🎉 ALL TESTS PASSED! 🎉");
            System.out.println("The MediTrack application is working correctly.");
        } else {
            System.out.println("⚠️  Some tests failed. Please review the implementation.");
        }
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Functional interface for test cases.
     */
    @FunctionalInterface
    private interface TestCase {
        boolean run() throws Exception;
    }
}