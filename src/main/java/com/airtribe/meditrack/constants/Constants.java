package com.airtribe.meditrack.constants;

/**
 * Constants class containing application-wide configuration values.
 * This class demonstrates static initialization and constant definition.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public final class Constants {
    
    // Tax and billing constants
    public static final double TAX_RATE = 0.18; // 18% GST
    public static final double DISCOUNT_THRESHOLD = 5000.0;
    public static final double SENIOR_CITIZEN_DISCOUNT = 0.10; // 10% discount
    public static final double INSURANCE_DISCOUNT = 0.15; // 15% discount
    
    // File paths for data persistence
    public static final String DATA_DIRECTORY = "data/";
    public static final String PATIENTS_FILE = DATA_DIRECTORY + "patients.csv";
    public static final String DOCTORS_FILE = DATA_DIRECTORY + "doctors.csv";
    public static final String APPOINTMENTS_FILE = DATA_DIRECTORY + "appointments.csv";
    public static final String BILLS_FILE = DATA_DIRECTORY + "bills.csv";
    public static final String BACKUP_DIRECTORY = DATA_DIRECTORY + "backup/";
    
    // Application configuration
    public static final String APPLICATION_NAME = "MediTrack";
    public static final String VERSION = "1.0.0";
    public static final int DEFAULT_APPOINTMENT_DURATION = 30; // minutes
    public static final int MAX_APPOINTMENTS_PER_DAY = 20;
    public static final int CLINIC_START_HOUR = 9;
    public static final int CLINIC_END_HOUR = 18;
    
    // Validation constants
    public static final int MIN_AGE = 0;
    public static final int MAX_AGE = 150;
    public static final int MIN_PHONE_LENGTH = 10;
    public static final int MAX_PHONE_LENGTH = 15;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    
    // ID generation constants
    public static final String PATIENT_ID_PREFIX = "P";
    public static final String DOCTOR_ID_PREFIX = "D";
    public static final String APPOINTMENT_ID_PREFIX = "A";
    public static final String BILL_ID_PREFIX = "B";
    public static final int ID_LENGTH = 6; // Total length including prefix
    
    // Date and time formats
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    
    // Console UI constants
    public static final String MENU_SEPARATOR = "=" + "=".repeat(50);
    public static final String SUBMENU_SEPARATOR = "-" + "-".repeat(30);
    public static final int CONSOLE_WIDTH = 80;
    
    // Static initialization block
    static {
        System.out.println("[STATIC BLOCK] Constants class initialized at " + 
                          java.time.LocalDateTime.now());
        
        // Create data directory if it doesn't exist
        java.io.File dataDir = new java.io.File(DATA_DIRECTORY);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        java.io.File backupDir = new java.io.File(BACKUP_DIRECTORY);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
    
    /**
     * Application information getter methods
     */
    public static String getApplicationInfo() {
        return APPLICATION_NAME + " v" + VERSION;
    }
    
    /**
     * Get formatted tax rate as percentage
     * @return Tax rate as percentage string
     */
    public static String getTaxRatePercentage() {
        return String.format("%.1f%%", TAX_RATE * 100);
    }
    
    /**
     * Calculate tax amount for given base amount
     * @param baseAmount The base amount to calculate tax on
     * @return Tax amount
     */
    public static double calculateTax(double baseAmount) {
        return baseAmount * TAX_RATE;
    }
    
    /**
     * Check if amount qualifies for discount
     * @param amount The amount to check
     * @return true if qualifies for discount
     */
    public static boolean qualifiesForDiscount(double amount) {
        return amount >= DISCOUNT_THRESHOLD;
    }
}