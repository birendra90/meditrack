package com.airtribe.meditrack;

import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.exception.*;
import com.airtribe.meditrack.service.*;
import com.airtribe.meditrack.util.*;
import com.airtribe.meditrack.constants.Constants;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class with menu-driven console UI.
 * This demonstrates the complete application architecture and user interaction.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class Main {
    
    // Services
    private static DoctorService doctorService;
    private static PatientService patientService;
    private static AppointmentService appointmentService;
    
    // Utilities
    private static Scanner scanner;
    private static boolean loadDataOnStartup = false;
    
    /**
     * Main method - entry point of the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Check for command line arguments
        processCommandLineArgs(args);
        
        // Initialize the application
        initialize();
        
        // Show welcome message
        showWelcomeMessage();
        
        // Load data if requested
        if (loadDataOnStartup) {
            loadDataFromFiles();
        }
        
        // Start the main menu loop
        runMainMenu();
        
        // Cleanup and shutdown
        shutdown();
    }
    
    /**
     * Processes command line arguments.
     * 
     * @param args command line arguments
     */
    private static void processCommandLineArgs(String[] args) {
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "--loaddata":
                    loadDataOnStartup = true;
                    break;
                case "--help":
                    showHelp();
                    System.exit(0);
                    break;
                case "--version":
                    showVersion();
                    System.exit(0);
                    break;
            }
        }
    }
    
    /**
     * Shows help information.
     */
    private static void showHelp() {
        System.out.println("MediTrack - Medical Practice Management System");
        System.out.println("Usage: java com.airtribe.meditrack.Main [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --loadData    Load existing data from CSV files on startup");
        System.out.println("  --help        Show this help message");
        System.out.println("  --version     Show version information");
    }
    
    /**
     * Shows version information.
     */
    private static void showVersion() {
        System.out.println(Constants.getApplicationInfo());
        System.out.println("Java Implementation of Medical Practice Management System");
        System.out.println("Demonstrating OOP concepts, design patterns, and best practices");
    }
    
    /**
     * Initializes the application services and resources.
     */
    private static void initialize() {
        System.out.println("Initializing " + Constants.getApplicationInfo() + "...");
        
        // Initialize scanner for user input
        scanner = new Scanner(System.in);
        
        // Initialize services
        doctorService = new DoctorService();
        patientService = new PatientService();
        appointmentService = new AppointmentService(doctorService, patientService);
        
        System.out.println("Application initialized successfully!");
    }
    
    /**
     * Shows the welcome message and system information.
     */
    private static void showWelcomeMessage() {
        System.out.println();
        System.out.println(Constants.MENU_SEPARATOR);
        System.out.println("    Welcome to " + Constants.getApplicationInfo());
        System.out.println("    Medical Practice Management System");
        System.out.println(Constants.MENU_SEPARATOR);
        System.out.println();
        
        // Show current system status
        System.out.println("System Status:");
        System.out.printf("  Doctors: %d\n", doctorService.getActiveDoctorCount());
        System.out.printf("  Patients: %d\n", patientService.getActivePatientCount());
        System.out.printf("  Appointments: %d\n", appointmentService.getActiveAppointmentCount());
        System.out.println("  System Time: " + LocalDateTime.now());
        System.out.println();
    }
    
    /**
     * Loads data from CSV files.
     */
    private static void loadDataFromFiles() {
        System.out.println("Loading data from CSV files...");
        
        try {
            // Load doctors
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(Constants.DOCTORS_FILE))) {
                List<Doctor> doctors = CSVUtil.loadDoctors(Constants.DOCTORS_FILE);
                for (Doctor doctor : doctors) {
                    doctorService.getDataStore().store(doctor.getId(), doctor);
                }
                System.out.println("Loaded " + doctors.size() + " doctors");
            }
            
            // Load patients
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(Constants.PATIENTS_FILE))) {
                List<Patient> patients = CSVUtil.loadPatients(Constants.PATIENTS_FILE);
                for (Patient patient : patients) {
                    patientService.getDataStore().store(patient.getId(), patient);
                }
                System.out.println("Loaded " + patients.size() + " patients");
            }
            
            // Load appointments
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(Constants.APPOINTMENTS_FILE))) {
                List<Appointment> appointments = CSVUtil.loadAppointments(Constants.APPOINTMENTS_FILE);
                for (Appointment appointment : appointments) {
                    appointmentService.getDataStore().store(appointment.getId(), appointment);
                }
                System.out.println("Loaded " + appointments.size() + " appointments");
            }
            
            System.out.println("Data loaded successfully!");
            
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
    
    /**
     * Runs the main menu loop.
     */
    private static void runMainMenu() {
        boolean running = true;
        
        while (running) {
            showMainMenu();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        doctorManagementMenu();
                        break;
                    case 2:
                        patientManagementMenu();
                        break;
                    case 3:
                        appointmentManagementMenu();
                        break;
                    case 4:
                        reportsAndStatistics();
                        break;
                    case 5:
                        dataManagementMenu();
                        break;
                    case 6:
                        systemUtilities();
                        break;
                    case 0:
                        running = confirmExit();
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("Please try again.");
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }
    
    /**
     * Shows the main menu options.
     */
    private static void showMainMenu() {
        System.out.println();
        System.out.println(Constants.MENU_SEPARATOR);
        System.out.println("                 MAIN MENU");
        System.out.println(Constants.MENU_SEPARATOR);
        System.out.println("1. Doctor Management");
        System.out.println("2. Patient Management");
        System.out.println("3. Appointment Management");
        System.out.println("4. Reports and Statistics");
        System.out.println("5. Data Management");
        System.out.println("6. System Utilities");
        System.out.println("0. Exit");
        System.out.println(Constants.MENU_SEPARATOR);
    }
    
    // Doctor Management Methods
    
    /**
     * Doctor management submenu.
     */
    private static void doctorManagementMenu() {
        boolean back = false;
        
        while (!back) {
            System.out.println();
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("        DOCTOR MANAGEMENT");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("1. Add New Doctor");
            System.out.println("2. View All Doctors");
            System.out.println("3. Search Doctors");
            System.out.println("4. Update Doctor");
            System.out.println("5. View Doctor Details");
            System.out.println("6. Doctor Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    addNewDoctor();
                    break;
                case 2:
                    viewAllDoctors();
                    break;
                case 3:
                    searchDoctors();
                    break;
                case 4:
                    updateDoctor();
                    break;
                case 5:
                    viewDoctorDetails();
                    break;
                case 6:
                    System.out.println(doctorService.getDoctorStatistics());
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    /**
     * Adds a new doctor.
     */
    private static void addNewDoctor() {
        System.out.println("\n--- Add New Doctor ---");
        
        try {
            System.out.print("First Name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("Last Name: ");
            String lastName = scanner.nextLine().trim();
            
            LocalDate dateOfBirth = getDateInput("Date of Birth (dd/MM/yyyy): ");
            
            System.out.print("Gender (Male/Female/Other): ");
            String gender = scanner.nextLine().trim();
            
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            
            System.out.print("Address: ");
            String address = scanner.nextLine().trim();
            
            System.out.print("License Number: ");
            String licenseNumber = scanner.nextLine().trim();
            
            // Show specializations
            System.out.println("\nAvailable Specializations:");
            Specialization[] specializations = Specialization.values();
            for (int i = 0; i < specializations.length; i++) {
                System.out.printf("%d. %s\n", i + 1, specializations[i].getDisplayName());
            }
            
            int specChoice = getIntInput("Select Specialization (1-" + specializations.length + "): ");
            if (specChoice < 1 || specChoice > specializations.length) {
                System.out.println("Invalid specialization choice.");
                return;
            }
            Specialization specialization = specializations[specChoice - 1];
            
            int experience = getIntInput("Years of Experience: ");
            
            System.out.print("Medical Qualification: ");
            String qualification = scanner.nextLine().trim();
            
            Doctor doctor = doctorService.createDoctor(firstName, lastName, dateOfBirth, gender,
                    email, phone, address, licenseNumber, specialization, experience, qualification);
            
            System.out.println("\nDoctor created successfully!");
            System.out.println("Doctor ID: " + doctor.getId());
            System.out.println("Name: " + doctor.getFullName());
            System.out.println("Specialization: " + doctor.getSpecialization().getDisplayName());
            
        } catch (InvalidDataException e) {
            System.err.println("Error creating doctor: " + e.getMessage());
        }
    }
    
    /**
     * Views all doctors.
     */
    private static void viewAllDoctors() {
        System.out.println("\n--- All Doctors ---");
        
        List<Doctor> doctors = doctorService.getAllDoctorsSorted();
        
        if (doctors.isEmpty()) {
            System.out.println("No doctors found.");
            return;
        }
        
        System.out.printf("%-8s %-25s %-20s %-10s %-12s %-10s\n", 
                "ID", "Name", "Specialization", "Experience", "Fee", "Rating");
        System.out.println("-".repeat(95));
        
        for (Doctor doctor : doctors) {
            System.out.printf("%-8s %-25s %-20s %-10d ₹%-11.2f %-10.1f\n",
                    doctor.getId(),
                    truncate(doctor.getFullName(), 25),
                    truncate(doctor.getSpecialization().getDisplayName(), 20),
                    doctor.getYearsOfExperience(),
                    doctor.getConsultationFee(),
                    doctor.getRating());
        }
        
        System.out.println("\nTotal doctors: " + doctors.size());
    }
    
    /**
     * Searches for doctors.
     */
    private static void searchDoctors() {
        System.out.println("\n--- Search Doctors ---");
        System.out.print("Enter search term (name, specialization, etc.): ");
        String searchTerm = scanner.nextLine().trim();
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }
        
        List<Doctor> results = doctorService.searchDoctors(searchTerm);
        
        if (results.isEmpty()) {
            System.out.println("No doctors found matching: " + searchTerm);
            return;
        }
        
        System.out.println("\nSearch Results (" + results.size() + " found):");
        System.out.printf("%-8s %-25s %-20s %-10s\n", "ID", "Name", "Specialization", "Available");
        System.out.println("-".repeat(70));
        
        for (Doctor doctor : results) {
            System.out.printf("%-8s %-25s %-20s %-10s\n",
                    doctor.getId(),
                    truncate(doctor.getFullName(), 25),
                    truncate(doctor.getSpecialization().getDisplayName(), 20),
                    doctor.isAvailable() ? "Yes" : "No");
        }
    }
    
    /**
     * Updates a doctor's information.
     */
    private static void updateDoctor() {
        System.out.println("\n--- Update Doctor ---");
        System.out.print("Enter Doctor ID: ");
        String doctorId = scanner.nextLine().trim();
        
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found with ID: " + doctorId);
            return;
        }
        
        System.out.println("Current Information:");
        System.out.println(doctor.getDetailedInfo());
        
        // Simple update - just availability for demonstration
        System.out.print("Update availability (current: " + doctor.isAvailable() + ") [y/n]: ");
        String updateAvailability = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(updateAvailability)) {
            doctor.setAvailable(!doctor.isAvailable());
            System.out.println("Availability updated to: " + doctor.isAvailable());
        }
    }
    
    /**
     * Views detailed information about a doctor.
     */
    private static void viewDoctorDetails() {
        System.out.println("\n--- Doctor Details ---");
        System.out.print("Enter Doctor ID: ");
        String doctorId = scanner.nextLine().trim();
        
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found with ID: " + doctorId);
            return;
        }
        
        System.out.println(doctor.getDetailedInfo());
    }
    
    // Patient Management Methods
    
    /**
     * Patient management submenu.
     */
    private static void patientManagementMenu() {
        boolean back = false;
        
        while (!back) {
            System.out.println();
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("       PATIENT MANAGEMENT");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("1. Add New Patient");
            System.out.println("2. View All Patients");
            System.out.println("3. Search Patients");
            System.out.println("4. Update Patient");
            System.out.println("5. View Patient Details");
            System.out.println("6. Patient Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    addNewPatient();
                    break;
                case 2:
                    viewAllPatients();
                    break;
                case 3:
                    searchPatients();
                    break;
                case 4:
                    updatePatient();
                    break;
                case 5:
                    viewPatientDetails();
                    break;
                case 6:
                    System.out.println(patientService.getPatientStatistics());
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    /**
     * Adds a new patient.
     */
    private static void addNewPatient() {
        System.out.println("\n--- Add New Patient ---");
        
        try {
            System.out.print("First Name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("Last Name: ");
            String lastName = scanner.nextLine().trim();
            
            LocalDate dateOfBirth = getDateInput("Date of Birth (dd/MM/yyyy): ");
            
            System.out.print("Gender (Male/Female/Other): ");
            String gender = scanner.nextLine().trim();
            
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            
            System.out.print("Address: ");
            String address = scanner.nextLine().trim();
            
            System.out.println("Patient Type:");
            System.out.println("1. OUTPATIENT");
            System.out.println("2. INPATIENT");
            System.out.println("3. EMERGENCY");
            int typeChoice = getIntInput("Select Patient Type (1-3): ");
            
            String patientType;
            switch (typeChoice) {
                case 1: patientType = "OUTPATIENT"; break;
                case 2: patientType = "INPATIENT"; break;
                case 3: patientType = "EMERGENCY"; break;
                default:
                    System.out.println("Invalid choice. Defaulting to OUTPATIENT.");
                    patientType = "OUTPATIENT";
            }
            
            System.out.print("Insurance Provider (optional): ");
            String insuranceProvider = scanner.nextLine().trim();
            if (insuranceProvider.isEmpty()) {
                insuranceProvider = null;
            }
            
            Patient patient = patientService.createPatient(firstName, lastName, dateOfBirth, gender,
                    email, phone, address, patientType, insuranceProvider);
            
            System.out.println("\nPatient created successfully!");
            System.out.println("Patient ID: " + patient.getId());
            System.out.println("Name: " + patient.getFullName());
            System.out.println("Age: " + patient.getAge() + " years");
            System.out.println("Type: " + patient.getPatientType());
            
        } catch (InvalidDataException e) {
            System.err.println("Error creating patient: " + e.getMessage());
        }
    }
    
    /**
     * Views all patients.
     */
    private static void viewAllPatients() {
        System.out.println("\n--- All Patients ---");
        
        List<Patient> patients = patientService.getAllPatientsSorted();
        
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
            return;
        }
        
        System.out.printf("%-8s %-25s %-5s %-12s %-10s %-7s\n", 
                "ID", "Name", "Age", "Type", "Insurance", "Visits");
        System.out.println("-".repeat(75));
        
        for (Patient patient : patients) {
            System.out.printf("%-8s %-25s %-5d %-12s %-10s %-7d\n",
                    patient.getId(),
                    truncate(patient.getFullName(), 25),
                    patient.getAge(),
                    truncate(patient.getPatientType(), 12),
                    patient.hasInsurance() ? "Yes" : "No",
                    patient.getVisitCount());
        }
        
        System.out.println("\nTotal patients: " + patients.size());
    }
    
    /**
     * Searches for patients.
     */
    private static void searchPatients() {
        System.out.println("\n--- Search Patients ---");
        System.out.print("Enter search term (name, ID, etc.): ");
        String searchTerm = scanner.nextLine().trim();
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }
        
        List<Patient> results = patientService.searchPatients(searchTerm);
        
        if (results.isEmpty()) {
            System.out.println("No patients found matching: " + searchTerm);
            return;
        }
        
        System.out.println("\nSearch Results (" + results.size() + " found):");
        System.out.printf("%-8s %-25s %-5s %-12s\n", "ID", "Name", "Age", "Type");
        System.out.println("-".repeat(55));
        
        for (Patient patient : results) {
            System.out.printf("%-8s %-25s %-5d %-12s\n",
                    patient.getId(),
                    truncate(patient.getFullName(), 25),
                    patient.getAge(),
                    patient.getPatientType());
        }
    }
    
    /**
     * Updates a patient's information.
     */
    private static void updatePatient() {
        System.out.println("\n--- Update Patient ---");
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine().trim();
        
        Patient patient = patientService.getPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found with ID: " + patientId);
            return;
        }
        
        System.out.println("Current Information:");
        System.out.println(patient.getDetailedInfo());
        
        System.out.print("Add medical history item (or press Enter to skip): ");
        String historyItem = scanner.nextLine().trim();
        
        if (!historyItem.isEmpty()) {
            try {
                patientService.addMedicalHistory(patientId, historyItem);
                System.out.println("Medical history updated.");
            } catch (InvalidDataException e) {
                System.err.println("Error updating medical history: " + e.getMessage());
            }
        }
    }
    
    /**
     * Views detailed information about a patient.
     */
    private static void viewPatientDetails() {
        System.out.println("\n--- Patient Details ---");
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine().trim();
        
        Patient patient = patientService.getPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found with ID: " + patientId);
            return;
        }
        
        System.out.println(patient.getDetailedInfo());
    }
    
    // Appointment Management Methods
    
    /**
     * Appointment management submenu.
     */
    private static void appointmentManagementMenu() {
        boolean back = false;
        
        while (!back) {
            System.out.println();
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("    APPOINTMENT MANAGEMENT");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("1. Book New Appointment");
            System.out.println("2. View All Appointments");
            System.out.println("3. Today's Appointments");
            System.out.println("4. Search Appointments");
            System.out.println("5. Update Appointment Status");
            System.out.println("6. Cancel Appointment");
            System.out.println("7. Appointment Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    bookNewAppointment();
                    break;
                case 2:
                    viewAllAppointments();
                    break;
                case 3:
                    viewTodaysAppointments();
                    break;
                case 4:
                    searchAppointments();
                    break;
                case 5:
                    updateAppointmentStatus();
                    break;
                case 6:
                    cancelAppointment();
                    break;
                case 7:
                    System.out.println(appointmentService.getAppointmentStatistics());
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    /**
     * Books a new appointment.
     */
    private static void bookNewAppointment() {
        System.out.println("\n--- Book New Appointment ---");
        
        try {
            System.out.print("Patient ID: ");
            String patientId = scanner.nextLine().trim();
            
            System.out.print("Doctor ID: ");
            String doctorId = scanner.nextLine().trim();
            
            LocalDateTime appointmentDateTime = getDateTimeInput("Appointment Date and Time (dd/MM/yyyy HH:mm): ");
            
            int duration = getIntInput("Duration in minutes (default 30): ");
            if (duration <= 0) {
                duration = 30;
            }
            
            System.out.print("Reason for visit: ");
            String reasonForVisit = scanner.nextLine().trim();
            
            System.out.println("Appointment Type:");
            System.out.println("1. CONSULTATION");
            System.out.println("2. FOLLOW_UP");
            System.out.println("3. CHECKUP");
            System.out.println("4. EMERGENCY");
            int typeChoice = getIntInput("Select type (1-4): ");
            
            String appointmentType;
            boolean isEmergency = false;
            switch (typeChoice) {
                case 1: appointmentType = "CONSULTATION"; break;
                case 2: appointmentType = "FOLLOW_UP"; break;
                case 3: appointmentType = "CHECKUP"; break;
                case 4: 
                    appointmentType = "EMERGENCY"; 
                    isEmergency = true; 
                    break;
                default:
                    appointmentType = "CONSULTATION";
            }
            
            Appointment appointment = appointmentService.createAppointment(patientId, doctorId,
                    appointmentDateTime, duration, reasonForVisit, appointmentType, isEmergency);
            
            System.out.println("\nAppointment booked successfully!");
            System.out.println("Appointment ID: " + appointment.getId());
            System.out.println("Date/Time: " + DateUtil.formatDateTime(appointment.getAppointmentDateTime()));
            System.out.println("Status: " + appointment.getStatus().getDisplayName());
            System.out.println("Fee: ₹" + String.format("%.2f", appointment.getConsultationFee()));
            
        } catch (InvalidDataException e) {
            System.err.println("Error booking appointment: " + e.getMessage());
        }
    }
    
    /**
     * Views all appointments.
     */
    private static void viewAllAppointments() {
        System.out.println("\n--- All Appointments ---");
        
        List<Appointment> appointments = appointmentService.getAllAppointmentsSorted();
        
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }
        
        System.out.printf("%-8s %-10s %-10s %-17s %-12s %-10s\n", 
                "ID", "Patient", "Doctor", "Date/Time", "Status", "Type");
        System.out.println("-".repeat(80));
        
        for (Appointment appointment : appointments) {
            System.out.printf("%-8s %-10s %-10s %-17s %-12s %-10s\n",
                    appointment.getId(),
                    appointment.getPatientId(),
                    appointment.getDoctorId(),
                    DateUtil.formatDateTime(appointment.getAppointmentDateTime()),
                    truncate(appointment.getStatus().getDisplayName(), 12),
                    truncate(appointment.getAppointmentType(), 10));
        }
        
        System.out.println("\nTotal appointments: " + appointments.size());
    }
    
    /**
     * Views today's appointments.
     */
    private static void viewTodaysAppointments() {
        System.out.println("\n--- Today's Appointments ---");
        
        List<Appointment> appointments = appointmentService.getTodaysAppointments();
        
        if (appointments.isEmpty()) {
            System.out.println("No appointments scheduled for today.");
            return;
        }
        
        System.out.printf("%-8s %-10s %-10s %-8s %-12s %-15s\n", 
                "ID", "Patient", "Doctor", "Time", "Status", "Reason");
        System.out.println("-".repeat(80));
        
        for (Appointment appointment : appointments) {
            String time = appointment.getAppointmentDateTime().toLocalTime().toString();
            System.out.printf("%-8s %-10s %-10s %-8s %-12s %-15s\n",
                    appointment.getId(),
                    appointment.getPatientId(),
                    appointment.getDoctorId(),
                    time,
                    truncate(appointment.getStatus().getDisplayName(), 12),
                    truncate(appointment.getReasonForVisit(), 15));
        }
        
        System.out.println("\nTotal today's appointments: " + appointments.size());
    }
    
    /**
     * Searches for appointments.
     */
    private static void searchAppointments() {
        System.out.println("\n--- Search Appointments ---");
        System.out.print("Enter search term (ID, patient ID, doctor ID, etc.): ");
        String searchTerm = scanner.nextLine().trim();
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }
        
        List<Appointment> results = appointmentService.searchAppointments(searchTerm);
        
        if (results.isEmpty()) {
            System.out.println("No appointments found matching: " + searchTerm);
            return;
        }
        
        System.out.println("\nSearch Results (" + results.size() + " found):");
        System.out.printf("%-8s %-10s %-17s %-12s\n", "ID", "Patient", "Date/Time", "Status");
        System.out.println("-".repeat(60));
        
        for (Appointment appointment : results) {
            System.out.printf("%-8s %-10s %-17s %-12s\n",
                    appointment.getId(),
                    appointment.getPatientId(),
                    DateUtil.formatDateTime(appointment.getAppointmentDateTime()),
                    appointment.getStatus().getDisplayName());
        }
    }
    
    /**
     * Updates appointment status.
     */
    private static void updateAppointmentStatus() {
        System.out.println("\n--- Update Appointment Status ---");
        System.out.print("Enter Appointment ID: ");
        String appointmentId = scanner.nextLine().trim();
        
        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            System.out.println("Current Status: " + appointment.getStatus().getDisplayName());
            
            System.out.println("\nAvailable Actions:");
            System.out.println("1. Confirm");
            System.out.println("2. Start");
            System.out.println("3. Complete");
            System.out.println("4. Cancel");
            System.out.println("5. Mark as No Show");
            
            int action = getIntInput("Select action (1-5): ");
            
            switch (action) {
                case 1:
                    appointmentService.confirmAppointment(appointmentId);
                    System.out.println("Appointment confirmed.");
                    break;
                case 2:
                    appointmentService.startAppointment(appointmentId);
                    System.out.println("Appointment started.");
                    break;
                case 3:
                    System.out.print("Diagnosis: ");
                    String diagnosis = scanner.nextLine().trim();
                    System.out.print("Prescription (optional): ");
                    String prescription = scanner.nextLine().trim();
                    System.out.print("Notes (optional): ");
                    String notes = scanner.nextLine().trim();
                    
                    appointmentService.completeAppointment(appointmentId, diagnosis, 
                            prescription.isEmpty() ? null : prescription,
                            notes.isEmpty() ? null : notes);
                    System.out.println("Appointment completed.");
                    break;
                case 4:
                    System.out.print("Cancellation reason: ");
                    String reason = scanner.nextLine().trim();
                    appointmentService.cancelAppointment(appointmentId, reason);
                    System.out.println("Appointment cancelled.");
                    break;
                case 5:
                    appointmentService.markNoShow(appointmentId);
                    System.out.println("Appointment marked as no-show.");
                    break;
                default:
                    System.out.println("Invalid action.");
            }
            
        } catch (AppointmentNotFoundException | InvalidDataException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Cancels an appointment.
     */
    private static void cancelAppointment() {
        System.out.println("\n--- Cancel Appointment ---");
        System.out.print("Enter Appointment ID: ");
        String appointmentId = scanner.nextLine().trim();
        
        System.out.print("Cancellation reason: ");
        String reason = scanner.nextLine().trim();
        
        try {
            appointmentService.cancelAppointment(appointmentId, reason);
            System.out.println("Appointment cancelled successfully.");
        } catch (AppointmentNotFoundException | InvalidDataException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
        }
    }
    
    // Reports and Statistics
    
    /**
     * Shows reports and statistics menu.
     */
    private static void reportsAndStatistics() {
        System.out.println("\n--- Reports and Statistics ---");
        System.out.println("1. System Overview");
        System.out.println("2. Doctor Statistics");
        System.out.println("3. Patient Statistics");
        System.out.println("4. Appointment Statistics");
        
        int choice = getIntInput("Select report (1-4): ");
        
        switch (choice) {
            case 1:
                showSystemOverview();
                break;
            case 2:
                System.out.println(doctorService.getDoctorStatistics());
                break;
            case 3:
                System.out.println(patientService.getPatientStatistics());
                break;
            case 4:
                System.out.println(appointmentService.getAppointmentStatistics());
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    /**
     * Shows system overview.
     */
    private static void showSystemOverview() {
        System.out.println("\n" + Constants.MENU_SEPARATOR);
        System.out.println("             SYSTEM OVERVIEW");
        System.out.println(Constants.MENU_SEPARATOR);
        
        System.out.println("System: " + Constants.getApplicationInfo());
        System.out.println("Current Time: " + LocalDateTime.now());
        System.out.println();
        
        System.out.printf("Active Doctors: %d\n", doctorService.getActiveDoctorCount());
        System.out.printf("Active Patients: %d\n", patientService.getActivePatientCount());
        System.out.printf("Total Appointments: %d\n", appointmentService.getActiveAppointmentCount());
        System.out.printf("Today's Appointments: %d\n", appointmentService.getTodaysAppointments().size());
        System.out.printf("Upcoming Appointments: %d\n", appointmentService.getUpcomingAppointments().size());
        
        System.out.println(Constants.MENU_SEPARATOR);
    }
    
    // Data Management
    
    /**
     * Data management submenu.
     */
    private static void dataManagementMenu() {
        boolean back = false;
        
        while (!back) {
            System.out.println();
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("        DATA MANAGEMENT");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            System.out.println("1. Export Data to CSV");
            System.out.println("2. Import Data from CSV");
            System.out.println("3. Backup Data");
            System.out.println("4. Validate Data");
            System.out.println("0. Back to Main Menu");
            System.out.println(Constants.SUBMENU_SEPARATOR);
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    exportDataToCSV();
                    break;
                case 2:
                    importDataFromCSV();
                    break;
                case 3:
                    backupData();
                    break;
                case 4:
                    validateData();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    /**
     * Exports data to CSV files.
     */
    private static void exportDataToCSV() {
        System.out.println("\n--- Export Data to CSV ---");
        
        try {
            // Export doctors
            List<Doctor> doctors = doctorService.getAllDoctors();
            CSVUtil.saveDoctors(doctors, Constants.DOCTORS_FILE);
            System.out.println("Exported " + doctors.size() + " doctors to " + Constants.DOCTORS_FILE);
            
            // Export patients
            List<Patient> patients = patientService.getAllPatients();
            CSVUtil.savePatients(patients, Constants.PATIENTS_FILE);
            System.out.println("Exported " + patients.size() + " patients to " + Constants.PATIENTS_FILE);
            
            // Export appointments
            List<Appointment> appointments = appointmentService.getAllAppointments();
            CSVUtil.saveAppointments(appointments, Constants.APPOINTMENTS_FILE);
            System.out.println("Exported " + appointments.size() + " appointments to " + Constants.APPOINTMENTS_FILE);
            
            System.out.println("Data export completed successfully!");
            
        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }
    
    /**
     * Imports data from CSV files.
     */
    private static void importDataFromCSV() {
        System.out.println("\n--- Import Data from CSV ---");
        System.out.println("Warning: This will overwrite existing data!");
        System.out.print("Continue? (y/n): ");
        
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!"y".equals(confirm)) {
            System.out.println("Import cancelled.");
            return;
        }
        
        loadDataFromFiles();
    }
    
    /**
     * Creates backup of current data.
     */
    private static void backupData() {
        System.out.println("\n--- Backup Data ---");
        
        try {
            CSVUtil.createBackup();
            System.out.println("Backup created successfully!");
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }
    
    /**
     * Validates all data in the system.
     */
    private static void validateData() {
        System.out.println("\n--- Data Validation ---");
        
        // Validate doctors
        DataStore.ValidationResult doctorValidation = doctorService.validateAllDoctors();
        System.out.println("Doctor Validation:");
        System.out.println(doctorValidation);
        
        // Validate patients
        DataStore.ValidationResult patientValidation = patientService.validateAllPatients();
        System.out.println("Patient Validation:");
        System.out.println(patientValidation);
        
        System.out.println("Data validation completed.");
    }
    
    // System Utilities
    
    /**
     * System utilities menu.
     */
    private static void systemUtilities() {
        System.out.println("\n--- System Utilities ---");
        System.out.println("1. ID Generator Demo");
        System.out.println("2. Send Appointment Reminders");
        System.out.println("3. System Information");
        
        int choice = getIntInput("Select utility (1-3): ");
        
        switch (choice) {
            case 1:
                System.out.println(IdGenerator.getInstance().demonstrateIdGeneration());
                break;
            case 2:
                List<Appointment> reminders = appointmentService.sendAppointmentReminders();
                System.out.println("Sent " + reminders.size() + " appointment reminders.");
                break;
            case 3:
                showSystemInformation();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    /**
     * Shows system information.
     */
    private static void showSystemInformation() {
        System.out.println("\n--- System Information ---");
        System.out.println("Application: " + Constants.getApplicationInfo());
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Operating System: " + System.getProperty("os.name"));
        System.out.println("Available Memory: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB");
        System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB");
        
        System.out.println("\nData Store Statistics:");
        System.out.println(doctorService.getDataStore().getStatistics());
        System.out.println(patientService.getDataStore().getStatistics());
        System.out.println(appointmentService.getDataStore().getStatistics());
    }
    
    // Utility Methods
    
    /**
     * Gets integer input from user with validation.
     */
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Gets date input from user.
     */
    private static LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return DateUtil.parseDate(input);
            } catch (Exception e) {
                System.out.println("Please enter date in format dd/MM/yyyy (e.g., 15/03/1990)");
            }
        }
    }
    
    /**
     * Gets date-time input from user.
     */
    private static LocalDateTime getDateTimeInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return DateUtil.parseDateTime(input);
            } catch (Exception e) {
                System.out.println("Please enter date-time in format dd/MM/yyyy HH:mm (e.g., 15/03/2025 14:30)");
            }
        }
    }
    
    /**
     * Truncates a string to specified length.
     */
    private static String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() <= length ? str : str.substring(0, length - 3) + "...";
    }
    
    /**
     * Confirms exit from application.
     */
    private static boolean confirmExit() {
        System.out.print("Are you sure you want to exit? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return "y".equals(input) || "yes".equals(input);
    }
    
    /**
     * Cleanup and shutdown the application.
     */
    private static void shutdown() {
        System.out.println("\nSaving data before exit...");
        
        // Auto-save data
        try {
            exportDataToCSV();
        } catch (Exception e) {
            System.err.println("Warning: Could not save data: " + e.getMessage());
        }
        
        System.out.println("Thank you for using " + Constants.getApplicationInfo());
        System.out.println("Goodbye!");
        
        // Close scanner
        if (scanner != null) {
            scanner.close();
        }
    }
}