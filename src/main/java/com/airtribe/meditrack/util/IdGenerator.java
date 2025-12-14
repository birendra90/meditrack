package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;

import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ID Generator utility class demonstrating Singleton pattern (both eager and lazy).
 * This class provides thread-safe ID generation for different entity types.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public final class IdGenerator {
    
    // Eager Singleton instance (initialized at class loading)
    private static final IdGenerator INSTANCE = new IdGenerator();
    
    // Thread-safe counters for different entity types
    private final Map<String, AtomicInteger> counters;
    
    // Date formatter for timestamp-based IDs
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * Private constructor for Singleton pattern.
     * Initializes counters for different entity types.
     */
    private IdGenerator() {
        this.counters = new ConcurrentHashMap<>();
        initializeCounters();
    }
    
    /**
     * Gets the singleton instance (Eager initialization).
     * 
     * @return the singleton instance
     */
    public static IdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initializes counters for all entity types.
     */
    private void initializeCounters() {
        counters.put("PATIENT", new AtomicInteger(0));
        counters.put("DOCTOR", new AtomicInteger(0));
        counters.put("APPOINTMENT", new AtomicInteger(0));
        counters.put("BILL", new AtomicInteger(0));
        counters.put("SUMMARY", new AtomicInteger(0));
    }
    
    /**
     * Generates a new ID for patients.
     * Format: P00001, P00002, etc.
     * 
     * @return new patient ID
     */
    public String generatePatientId() {
        return generateId(Constants.PATIENT_ID_PREFIX, "PATIENT");
    }
    
    /**
     * Generates a new ID for doctors.
     * Format: D00001, D00002, etc.
     * 
     * @return new doctor ID
     */
    public String generateDoctorId() {
        return generateId(Constants.DOCTOR_ID_PREFIX, "DOCTOR");
    }
    
    /**
     * Generates a new ID for appointments.
     * Format: A00001, A00002, etc.
     * 
     * @return new appointment ID
     */
    public String generateAppointmentId() {
        return generateId(Constants.APPOINTMENT_ID_PREFIX, "APPOINTMENT");
    }
    
    /**
     * Generates a new ID for bills.
     * Format: B00001, B00002, etc.
     * 
     * @return new bill ID
     */
    public String generateBillId() {
        return generateId(Constants.BILL_ID_PREFIX, "BILL");
    }
    
    /**
     * Generates a new ID for bill summaries.
     * Format: S00001, S00002, etc.
     * 
     * @return new summary ID
     */
    public String generateSummaryId() {
        return generateId("S", "SUMMARY");
    }
    
    /**
     * Generic method to generate IDs with specific prefix.
     * 
     * @param prefix the ID prefix
     * @param counterKey the counter key for this entity type
     * @return formatted ID
     */
    private String generateId(String prefix, String counterKey) {
        AtomicInteger counter = counters.get(counterKey);
        if (counter == null) {
            counter = new AtomicInteger(0);
            counters.put(counterKey, counter);
        }
        
        int nextId = counter.incrementAndGet();
        int numberLength = Constants.ID_LENGTH - prefix.length();
        String format = "%s%0" + numberLength + "d";
        return String.format(format, prefix, nextId);
    }
    
    /**
     * Generates a timestamp-based ID.
     * Format: PREFIX_YYYYMMDDHHMMSS_COUNTER
     * 
     * @param prefix the ID prefix
     * @return timestamp-based ID
     */
    public String generateTimestampId(String prefix) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        AtomicInteger counter = counters.computeIfAbsent("TIMESTAMP", k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();
        return String.format("%s_%s_%03d", prefix, timestamp, count % 1000);
    }
    
    /**
     * Generates a UUID-like ID for unique identification.
     * 
     * @return UUID-like string
     */
    public String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Resets a specific counter (useful for testing).
     * 
     * @param counterKey the counter key to reset
     * @param value the value to reset to
     */
    public synchronized void resetCounter(String counterKey, int value) {
        AtomicInteger counter = counters.get(counterKey);
        if (counter != null) {
            counter.set(value);
        }
    }
    
    /**
     * Gets the current value of a counter.
     * 
     * @param counterKey the counter key
     * @return current counter value
     */
    public int getCurrentCount(String counterKey) {
        AtomicInteger counter = counters.get(counterKey);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Sets the counter to a specific value to resume from existing data.
     * This is useful when loading data from files.
     * 
     * @param counterKey the counter key
     * @param maxExistingId the highest existing ID number
     */
    public synchronized void initializeFromExisting(String counterKey, int maxExistingId) {
        counters.put(counterKey, new AtomicInteger(maxExistingId));
    }
    
    /**
     * Validates if an ID follows the expected format.
     * 
     * @param id the ID to validate
     * @param expectedPrefix the expected prefix
     * @return true if ID format is valid
     */
    public boolean isValidIdFormat(String id, String expectedPrefix) {
        if (id == null || id.length() != Constants.ID_LENGTH) {
            return false;
        }
        
        if (!id.startsWith(expectedPrefix)) {
            return false;
        }
        
        String numberPart = id.substring(expectedPrefix.length());
        try {
            Integer.parseInt(numberPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Extracts the numeric part of an ID.
     * 
     * @param id the ID to extract from
     * @param prefix the expected prefix
     * @return the numeric part, or -1 if invalid
     */
    public int extractIdNumber(String id, String prefix) {
        if (!isValidIdFormat(id, prefix)) {
            return -1;
        }
        
        String numberPart = id.substring(prefix.length());
        try {
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Gets statistics about ID generation.
     * 
     * @return formatted statistics string
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("ID Generation Statistics:\n");
        stats.append("-".repeat(30)).append("\n");
        
        for (Map.Entry<String, AtomicInteger> entry : counters.entrySet()) {
            stats.append(String.format("%-12s: %6d\n", entry.getKey(), entry.getValue().get()));
        }
        
        return stats.toString();
    }
    
    /**
     * Lazy Singleton implementation (alternative approach).
     * This demonstrates another way to implement Singleton pattern.
     */
    public static class LazyIdGenerator {
        // Private static class for lazy initialization
        private static class LazyHolder {
            static final LazyIdGenerator INSTANCE = new LazyIdGenerator();
        }
        
        private final Map<String, AtomicInteger> counters;
        
        private LazyIdGenerator() {
            this.counters = new ConcurrentHashMap<>();
            initializeCounters();
        }
        
        /**
         * Gets the lazy singleton instance.
         * 
         * @return the singleton instance
         */
        public static LazyIdGenerator getInstance() {
            return LazyHolder.INSTANCE;
        }
        
        private void initializeCounters() {
            counters.put("LAZY_PATIENT", new AtomicInteger(0));
            counters.put("LAZY_DOCTOR", new AtomicInteger(0));
            counters.put("LAZY_APPOINTMENT", new AtomicInteger(0));
            counters.put("LAZY_BILL", new AtomicInteger(0));
        }
        
        /**
         * Generates ID using lazy singleton approach.
         * 
         * @param prefix the ID prefix
         * @param counterKey the counter key
         * @return generated ID
         */
        public String generateId(String prefix, String counterKey) {
            AtomicInteger counter = counters.computeIfAbsent(counterKey, k -> new AtomicInteger(0));
            int nextId = counter.incrementAndGet();
            int numberLength = Constants.ID_LENGTH - prefix.length();
            String format = "%s%0" + numberLength + "d";
            return String.format(format, prefix, nextId);
        }
    }
    
    /**
     * Thread-safe ID Generator using double-checked locking (another Singleton approach).
     */
    public static class ThreadSafeIdGenerator {
        private static volatile ThreadSafeIdGenerator instance;
        private final Map<String, AtomicInteger> counters;
        
        private ThreadSafeIdGenerator() {
            this.counters = new ConcurrentHashMap<>();
        }
        
        /**
         * Gets the thread-safe singleton instance using double-checked locking.
         * 
         * @return the singleton instance
         */
        public static ThreadSafeIdGenerator getInstance() {
            if (instance == null) {
                synchronized (ThreadSafeIdGenerator.class) {
                    if (instance == null) {
                        instance = new ThreadSafeIdGenerator();
                    }
                }
            }
            return instance;
        }
        
        /**
         * Generates thread-safe ID.
         * 
         * @param prefix the ID prefix
         * @param counterKey the counter key
         * @return generated ID
         */
        public String generateId(String prefix, String counterKey) {
            AtomicInteger counter = counters.computeIfAbsent(counterKey, k -> new AtomicInteger(0));
            int nextId = counter.incrementAndGet();
            int numberLength = Constants.ID_LENGTH - prefix.length();
            String format = "%s%0" + numberLength + "d";
            return String.format(format, prefix, nextId);
        }
    }
    
    /**
     * Factory method to get appropriate ID generator based on requirements.
     * 
     * @param type the type of ID generator needed
     * @return the appropriate ID generator instance
     */
    public static Object getIdGenerator(GeneratorType type) {
        switch (type) {
            case EAGER:
                return IdGenerator.getInstance();
            case LAZY:
                return LazyIdGenerator.getInstance();
            case THREAD_SAFE:
                return ThreadSafeIdGenerator.getInstance();
            default:
                return IdGenerator.getInstance();
        }
    }
    
    /**
     * Enum for different types of ID generators.
     */
    public enum GeneratorType {
        EAGER,
        LAZY, 
        THREAD_SAFE
    }
    
    /**
     * Demo method to show different ID generation patterns.
     * 
     * @return demonstration output
     */
    public String demonstrateIdGeneration() {
        StringBuilder demo = new StringBuilder();
        demo.append("ID Generation Demonstration:\n");
        demo.append("=".repeat(40)).append("\n");
        
        // Generate sample IDs
        demo.append("Patient IDs:\n");
        for (int i = 0; i < 3; i++) {
            demo.append("  ").append(generatePatientId()).append("\n");
        }
        
        demo.append("\nDoctor IDs:\n");
        for (int i = 0; i < 3; i++) {
            demo.append("  ").append(generateDoctorId()).append("\n");
        }
        
        demo.append("\nAppointment IDs:\n");
        for (int i = 0; i < 3; i++) {
            demo.append("  ").append(generateAppointmentId()).append("\n");
        }
        
        demo.append("\nBill IDs:\n");
        for (int i = 0; i < 3; i++) {
            demo.append("  ").append(generateBillId()).append("\n");
        }
        
        demo.append("\nTimestamp IDs:\n");
        for (int i = 0; i < 2; i++) {
            demo.append("  ").append(generateTimestampId("REF")).append("\n");
        }
        
        demo.append("\nUnique IDs:\n");
        for (int i = 0; i < 2; i++) {
            demo.append("  ").append(generateUniqueId()).append("\n");
        }
        
        demo.append("\n").append(getStatistics());
        
        return demo.toString();
    }
    
    /**
     * Utility method to batch generate multiple IDs.
     * 
     * @param prefix the ID prefix
     * @param counterKey the counter key
     * @param count number of IDs to generate
     * @return array of generated IDs
     */
    public String[] generateBatch(String prefix, String counterKey, int count) {
        String[] ids = new String[count];
        for (int i = 0; i < count; i++) {
            ids[i] = generateId(prefix, counterKey);
        }
        return ids;
    }
    
    /**
     * Validates a batch of IDs for consistency.
     * 
     * @param ids the IDs to validate
     * @param expectedPrefix the expected prefix
     * @return validation result
     */
    public ValidationResult validateIdBatch(String[] ids, String expectedPrefix) {
        ValidationResult result = new ValidationResult();
        
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            
            if (!isValidIdFormat(id, expectedPrefix)) {
                result.addError("Invalid format at position " + i + ": " + id);
            }
            
            // Check for duplicates
            for (int j = i + 1; j < ids.length; j++) {
                if (id.equals(ids[j])) {
                    result.addError("Duplicate ID found: " + id + " at positions " + i + " and " + j);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Inner class for ID validation results.
     */
    public static class ValidationResult {
        private java.util.List<String> errors = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public java.util.List<String> getErrors() {
            return new java.util.ArrayList<>(errors);
        }
        
        @Override
        public String toString() {
            if (isValid()) {
                return "Validation: PASSED";
            } else {
                return "Validation: FAILED\nErrors:\n" + 
                       errors.stream().collect(java.util.stream.Collectors.joining("\n  - ", "  - ", ""));
            }
        }
    }
}