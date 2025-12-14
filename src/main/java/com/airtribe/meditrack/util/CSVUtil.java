package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.constants.Constants;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utility class for CSV file operations with try-with-resources.
 * This demonstrates file I/O, CSV parsing, and resource management.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public final class CSVUtil {
    
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final String NEW_LINE = System.lineSeparator();
    
    // Private constructor to prevent instantiation
    private CSVUtil() {
        throw new UnsupportedOperationException("CSVUtil is a utility class and cannot be instantiated");
    }
    
    // Generic CSV Operations
    
    /**
     * Reads all lines from a CSV file.
     * 
     * @param filePath the path to the CSV file
     * @return list of string arrays representing CSV rows
     * @throws IOException if file operations fail
     */
    public static List<String[]> readCSV(String filePath) throws IOException {
        List<String[]> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = parseCSVLine(line);
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    /**
     * Writes data to a CSV file.
     * 
     * @param filePath the path to the CSV file
     * @param headers the CSV headers
     * @param rows the data rows
     * @throws IOException if file operations fail
     */
    public static void writeCSV(String filePath, String[] headers, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write headers
            if (headers != null && headers.length > 0) {
                writer.write(formatCSVLine(headers));
                writer.write(NEW_LINE);
            }
            
            // Write data rows
            for (String[] row : rows) {
                writer.write(formatCSVLine(row));
                writer.write(NEW_LINE);
            }
        }
    }
    
    /**
     * Appends data to an existing CSV file.
     * 
     * @param filePath the path to the CSV file
     * @param rows the data rows to append
     * @throws IOException if file operations fail
     */
    public static void appendToCSV(String filePath, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String[] row : rows) {
                writer.write(formatCSVLine(row));
                writer.write(NEW_LINE);
            }
        }
    }
    
    /**
     * Parses a CSV line into individual fields.
     * Handles quoted fields and escaped quotes.
     * 
     * @param line the CSV line to parse
     * @return array of field values
     */
    public static String[] parseCSVLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new String[0];
        }
        
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        boolean quotedField = false;
        
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (ch == '"') {
                if (!inQuotes) {
                    // Starting a quoted field
                    inQuotes = true;
                    quotedField = true;
                } else {
                    // Check if this is an escaped quote
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        // Escaped quote - add one quote to field and skip next
                        currentField.append('"');
                        i++; // Skip the next quote
                    } else {
                        // End of quoted field
                        inQuotes = false;
                    }
                }
            } else if (ch == ',' && !inQuotes) {
                // Field separator
                fields.add(quotedField ? currentField.toString() : currentField.toString().trim());
                currentField = new StringBuilder();
                quotedField = false;
            } else {
                // Regular character
                currentField.append(ch);
            }
        }
        
        // Add the last field
        fields.add(quotedField ? currentField.toString() : currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * Formats an array of fields into a CSV line.
     * Automatically quotes fields that contain commas, quotes, or newlines.
     * 
     * @param fields the fields to format
     * @return formatted CSV line
     */
    public static String formatCSVLine(String[] fields) {
        if (fields == null || fields.length == 0) {
            return "";
        }
        
        StringBuilder line = new StringBuilder();
        
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                line.append(CSV_DELIMITER);
            }
            
            String field = fields[i] != null ? fields[i] : "";
            
            // Check if field needs quoting
            if (needsQuoting(field)) {
                line.append(CSV_QUOTE);
                line.append(field.replace(CSV_QUOTE, ESCAPED_QUOTE));
                line.append(CSV_QUOTE);
            } else {
                line.append(field);
            }
        }
        
        return line.toString();
    }
    
    /**
     * Checks if a field needs to be quoted in CSV.
     * 
     * @param field the field to check
     * @return true if field needs quoting
     */
    private static boolean needsQuoting(String field) {
        return field.contains(CSV_DELIMITER) || 
               field.contains(CSV_QUOTE) || 
               field.contains("\n") || 
               field.contains("\r");
    }
    
    // Entity-specific CSV operations
    
    /**
     * Saves patients to CSV file.
     * 
     * @param patients list of patients to save
     * @param filePath the file path to save to
     * @throws IOException if file operations fail
     */
    public static void savePatients(List<Patient> patients, String filePath) throws IOException {
        String[] headers = {
            "ID", "FirstName", "LastName", "DateOfBirth", "Gender", "Email", "Phone", 
            "Address", "BloodGroup", "PatientType", "InsuranceProvider", "HasInsurance",
            "RegistrationDate", "VisitCount", "GuardianName", "GuardianPhone"
        };
        
        List<String[]> rows = new ArrayList<>();
        
        for (Patient patient : patients) {
            String[] row = {
                nullSafe(patient.getId()),
                nullSafe(patient.getFirstName()),
                nullSafe(patient.getLastName()),
                formatDate(patient.getDateOfBirth()),
                nullSafe(patient.getGender()),
                nullSafe(patient.getEmail()),
                nullSafe(patient.getPhone()),
                nullSafe(patient.getAddress()),
                nullSafe(patient.getBloodGroup()),
                nullSafe(patient.getPatientType()),
                nullSafe(patient.getInsuranceProvider()),
                String.valueOf(patient.hasInsurance()),
                formatDate(patient.getRegistrationDate()),
                String.valueOf(patient.getVisitCount()),
                nullSafe(patient.getGuardianName()),
                nullSafe(patient.getGuardianPhone())
            };
            rows.add(row);
        }
        
        writeCSV(filePath, headers, rows);
    }
    
    /**
     * Loads patients from CSV file.
     * 
     * @param filePath the file path to load from
     * @return list of loaded patients
     * @throws IOException if file operations fail
     */
    public static List<Patient> loadPatients(String filePath) throws IOException {
        List<Patient> patients = new ArrayList<>();
        List<String[]> rows = readCSV(filePath);
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            
            if (row.length >= 16) {
                try {
                    Patient patient = new Patient();
                    patient.setId(row[0]);
                    patient.setFirstName(row[1]);
                    patient.setLastName(row[2]);
                    patient.setDateOfBirth(parseDate(row[3]));
                    patient.setGender(row[4]);
                    patient.setEmail(row[5]);
                    patient.setPhone(row[6]);
                    patient.setAddress(row[7]);
                    patient.setBloodGroup(row[8]);
                    patient.setPatientType(row[9]);
                    patient.setInsuranceProvider(row[10]);
                    patient.setRegistrationDate(parseDate(row[12]));
                    // visitCount is set in row[13] but we'll skip setting it directly
                    patient.setGuardianName(row[14]);
                    patient.setGuardianPhone(row[15]);
                    
                    patients.add(patient);
                } catch (Exception e) {
                    System.err.println("Error parsing patient row " + i + ": " + e.getMessage());
                }
            }
        }
        
        return patients;
    }
    
    /**
     * Saves doctors to CSV file.
     * 
     * @param doctors list of doctors to save
     * @param filePath the file path to save to
     * @throws IOException if file operations fail
     */
    public static void saveDoctors(List<Doctor> doctors, String filePath) throws IOException {
        String[] headers = {
            "ID", "FirstName", "LastName", "DateOfBirth", "Gender", "Email", "Phone",
            "Address", "LicenseNumber", "Specialization", "YearsOfExperience", 
            "ConsultationFee", "Qualification", "Department", "IsAvailable",
            "WorkingHours", "Chamber", "Rating", "TotalPatientsTreated"
        };
        
        List<String[]> rows = new ArrayList<>();
        
        for (Doctor doctor : doctors) {
            String[] row = {
                nullSafe(doctor.getId()),
                nullSafe(doctor.getFirstName()),
                nullSafe(doctor.getLastName()),
                formatDate(doctor.getDateOfBirth()),
                nullSafe(doctor.getGender()),
                nullSafe(doctor.getEmail()),
                nullSafe(doctor.getPhone()),
                nullSafe(doctor.getAddress()),
                nullSafe(doctor.getLicenseNumber()),
                doctor.getSpecialization() != null ? doctor.getSpecialization().name() : "",
                String.valueOf(doctor.getYearsOfExperience()),
                String.valueOf(doctor.getConsultationFee()),
                nullSafe(doctor.getQualification()),
                nullSafe(doctor.getDepartment()),
                String.valueOf(doctor.isAvailable()),
                nullSafe(doctor.getWorkingHours()),
                nullSafe(doctor.getChamber()),
                String.valueOf(doctor.getRating()),
                String.valueOf(doctor.getTotalPatientsTreated())
            };
            rows.add(row);
        }
        
        writeCSV(filePath, headers, rows);
    }
    
    /**
     * Loads doctors from CSV file.
     * 
     * @param filePath the file path to load from
     * @return list of loaded doctors
     * @throws IOException if file operations fail
     */
    public static List<Doctor> loadDoctors(String filePath) throws IOException {
        List<Doctor> doctors = new ArrayList<>();
        List<String[]> rows = readCSV(filePath);
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            
            if (row.length >= 19) {
                try {
                    Doctor doctor = new Doctor();
                    doctor.setId(row[0]);
                    doctor.setFirstName(row[1]);
                    doctor.setLastName(row[2]);
                    doctor.setDateOfBirth(parseDate(row[3]));
                    doctor.setGender(row[4]);
                    doctor.setEmail(row[5]);
                    doctor.setPhone(row[6]);
                    doctor.setAddress(row[7]);
                    doctor.setLicenseNumber(row[8]);
                    
                    // Parse specialization
                    Specialization specialization = Specialization.findByName(row[9]);
                    if (specialization != null) {
                        doctor.setSpecialization(specialization);
                    }
                    
                    doctor.setYearsOfExperience(parseInteger(row[10], 0));
                    doctor.setConsultationFee(parseDouble(row[11], 0.0));
                    doctor.setQualification(row[12]);
                    doctor.setDepartment(row[13]);
                    doctor.setAvailable(parseBoolean(row[14], true));
                    doctor.setWorkingHours(row[15]);
                    doctor.setChamber(row[16]);
                    doctor.setRating(parseDouble(row[17], 0.0));
                    
                    doctors.add(doctor);
                } catch (Exception e) {
                    System.err.println("Error parsing doctor row " + i + ": " + e.getMessage());
                }
            }
        }
        
        return doctors;
    }
    
    /**
     * Saves appointments to CSV file.
     * 
     * @param appointments list of appointments to save
     * @param filePath the file path to save to
     * @throws IOException if file operations fail
     */
    public static void saveAppointments(List<Appointment> appointments, String filePath) throws IOException {
        String[] headers = {
            "ID", "PatientID", "DoctorID", "DateTime", "Duration", "Status", "ReasonForVisit",
            "Notes", "Symptoms", "Diagnosis", "Prescription", "ConsultationFee", 
            "IsEmergency", "AppointmentType", "RescheduleCount", "CancellationReason"
        };
        
        List<String[]> rows = new ArrayList<>();
        
        for (Appointment appointment : appointments) {
            String[] row = {
                nullSafe(appointment.getId()),
                nullSafe(appointment.getPatientId()),
                nullSafe(appointment.getDoctorId()),
                formatDateTime(appointment.getAppointmentDateTime()),
                String.valueOf(appointment.getDurationMinutes()),
                appointment.getStatus() != null ? appointment.getStatus().name() : "",
                nullSafe(appointment.getReasonForVisit()),
                nullSafe(appointment.getNotes()),
                nullSafe(appointment.getSymptoms()),
                nullSafe(appointment.getDiagnosis()),
                nullSafe(appointment.getPrescription()),
                String.valueOf(appointment.getConsultationFee()),
                String.valueOf(appointment.isEmergency()),
                nullSafe(appointment.getAppointmentType()),
                String.valueOf(appointment.getRescheduleCount()),
                nullSafe(appointment.getCancellationReason())
            };
            rows.add(row);
        }
        
        writeCSV(filePath, headers, rows);
    }
    
    /**
     * Loads appointments from CSV file.
     * 
     * @param filePath the file path to load from
     * @return list of loaded appointments
     * @throws IOException if file operations fail
     */
    public static List<Appointment> loadAppointments(String filePath) throws IOException {
        List<Appointment> appointments = new ArrayList<>();
        List<String[]> rows = readCSV(filePath);
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            
            if (row.length >= 16) {
                try {
                    Appointment appointment = new Appointment();
                    appointment.setId(row[0]);
                    appointment.setPatientId(row[1]);
                    appointment.setDoctorId(row[2]);
                    appointment.setAppointmentDateTime(parseDateTime(row[3]));
                    appointment.setDurationMinutes(parseInteger(row[4], Constants.DEFAULT_APPOINTMENT_DURATION));
                    
                    // Parse status
                    AppointmentStatus status = AppointmentStatus.findByName(row[5]);
                    if (status != null) {
                        appointment.setStatus(status);
                    }
                    
                    appointment.setReasonForVisit(row[6]);
                    appointment.setNotes(row[7]);
                    appointment.setSymptoms(row[8]);
                    appointment.setDiagnosis(row[9]);
                    appointment.setPrescription(row[10]);
                    appointment.setConsultationFee(parseDouble(row[11], 0.0));
                    appointment.setEmergency(parseBoolean(row[12], false));
                    appointment.setAppointmentType(row[13]);
                    appointment.setCancellationReason(row[15]);
                    
                    appointments.add(appointment);
                } catch (Exception e) {
                    System.err.println("Error parsing appointment row " + i + ": " + e.getMessage());
                }
            }
        }
        
        return appointments;
    }
    
    /**
     * Saves bills to CSV file.
     * 
     * @param bills list of bills to save
     * @param filePath the file path to save to
     * @throws IOException if file operations fail
     */
    public static void saveBills(List<Bill> bills, String filePath) throws IOException {
        String[] headers = {
            "ID", "AppointmentID", "PatientID", "DoctorID", "BaseAmount", "DiscountAmount",
            "TaxAmount", "TotalAmount", "IsPaid", "PaymentDateTime", "PaymentMethod",
            "BillType", "InsuranceCoverage", "DueDate", "GeneratedBy"
        };
        
        List<String[]> rows = new ArrayList<>();
        
        for (Bill bill : bills) {
            String[] row = {
                nullSafe(bill.getId()),
                nullSafe(bill.getAppointmentId()),
                nullSafe(bill.getPatientId()),
                nullSafe(bill.getDoctorId()),
                String.valueOf(bill.getBaseAmount()),
                String.valueOf(bill.getDiscountAmount()),
                String.valueOf(bill.getTaxAmount()),
                String.valueOf(bill.calculateTotalAmount()),
                String.valueOf(bill.isPaid()),
                formatDateTime(bill.getPaymentDateTime()),
                nullSafe(bill.getPaymentMethod()),
                nullSafe(bill.getBillType()),
                String.valueOf(bill.getInsuranceCoverage()),
                formatDateTime(bill.getDueDate()),
                nullSafe(bill.getGeneratedBy())
            };
            rows.add(row);
        }
        
        writeCSV(filePath, headers, rows);
    }
    
    /**
     * Loads bills from CSV file.
     * 
     * @param filePath the file path to load from
     * @return list of loaded bills
     * @throws IOException if file operations fail
     */
    public static List<Bill> loadBills(String filePath) throws IOException {
        List<Bill> bills = new ArrayList<>();
        List<String[]> rows = readCSV(filePath);
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            
            if (row.length >= 15) {
                try {
                    String billId = row[0];
                    String appointmentId = row[1];
                    String patientId = row[2];
                    String doctorId = row[3];
                    double baseAmount = parseDouble(row[4], 0.0);
                    String billType = row[11];
                    
                    Bill bill = new Bill(billId, appointmentId, patientId, doctorId, baseAmount, billType);
                    
                    // Set additional properties
                    bill.setInsuranceCoverage(parseDouble(row[12], 0.0));
                    bill.setDueDate(parseDateTime(row[13]));
                    bill.setGeneratedBy(row[14]);
                    
                    // If bill is marked as paid, process payment
                    boolean isPaid = parseBoolean(row[8], false);
                    if (isPaid) {
                        LocalDateTime paymentDateTime = parseDateTime(row[9]);
                        String paymentMethod = row[10];
                        if (paymentDateTime != null && paymentMethod != null) {
                            bill.processPayment(bill.calculateTotalAmount(), paymentMethod, "CSV_IMPORT");
                        }
                    }
                    
                    bills.add(bill);
                } catch (Exception e) {
                    System.err.println("Error parsing bill row " + i + ": " + e.getMessage());
                }
            }
        }
        
        return bills;
    }
    
    // Helper methods for parsing and formatting
    
    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
    
    private static String formatDate(LocalDate date) {
        return date != null ? DateUtil.formatDate(date) : "";
    }
    
    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? DateUtil.formatDateTime(dateTime) : "";
    }
    
    private static LocalDate parseDate(String dateString) {
        return DateUtil.safeParse(dateString);
    }
    
    private static LocalDateTime parseDateTime(String dateTimeString) {
        return DateUtil.safeParseDateTime(dateTimeString);
    }
    
    private static int parseInteger(String intString, int defaultValue) {
        try {
            return intString != null && !intString.trim().isEmpty() ? Integer.parseInt(intString.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private static double parseDouble(String doubleString, double defaultValue) {
        try {
            return doubleString != null && !doubleString.trim().isEmpty() ? Double.parseDouble(doubleString.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private static boolean parseBoolean(String booleanString, boolean defaultValue) {
        if (booleanString == null || booleanString.trim().isEmpty()) {
            return defaultValue;
        }
        
        String trimmed = booleanString.trim().toLowerCase();
        return "true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed);
    }
    
    /**
     * Creates a backup of existing data files.
     * 
     * @throws IOException if backup operations fail
     */
    public static void createBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        // Create backup directory if it doesn't exist
        File backupDir = new File(Constants.BACKUP_DIRECTORY);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        // Backup each data file if it exists
        String[] dataFiles = {
            Constants.PATIENTS_FILE,
            Constants.DOCTORS_FILE, 
            Constants.APPOINTMENTS_FILE,
            Constants.BILLS_FILE
        };
        
        for (String dataFile : dataFiles) {
            File sourceFile = new File(dataFile);
            if (sourceFile.exists()) {
                String fileName = sourceFile.getName();
                String backupFileName = fileName.replace(".csv", "_" + timestamp + ".csv");
                File backupFile = new File(Constants.BACKUP_DIRECTORY + backupFileName);
                
                try (FileInputStream fis = new FileInputStream(sourceFile);
                     FileOutputStream fos = new FileOutputStream(backupFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                
                System.out.println("Backup created: " + backupFile.getAbsolutePath());
            }
        }
    }
    
    /**
     * Exports data to CSV with custom headers and formatting.
     * 
     * @param data the data to export
     * @param filePath the file path to export to
     * @param headers the custom headers
     * @param formatter the formatter function to convert objects to string arrays
     * @param <T> the type of data being exported
     * @throws IOException if export operations fail
     */
    public static <T> void exportData(List<T> data, String filePath, String[] headers, 
                                     DataFormatter<T> formatter) throws IOException {
        List<String[]> rows = new ArrayList<>();
        
        for (T item : data) {
            rows.add(formatter.format(item));
        }
        
        writeCSV(filePath, headers, rows);
    }
    
    /**
     * Functional interface for formatting data objects to CSV rows.
     * 
     * @param <T> the type of data being formatted
     */
    @FunctionalInterface
    public interface DataFormatter<T> {
        String[] format(T data);
    }
    
    /**
     * Validates CSV file structure.
     * 
     * @param filePath the file path to validate
     * @param expectedHeaders the expected headers
     * @return validation result with any issues found
     * @throws IOException if file operations fail
     */
    public static ValidationResult validateCSVStructure(String filePath, String[] expectedHeaders) throws IOException {
        ValidationResult result = new ValidationResult();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            
            if (headerLine == null) {
                result.addError("File is empty");
                return result;
            }
            
            String[] actualHeaders = parseCSVLine(headerLine);
            
            if (actualHeaders.length != expectedHeaders.length) {
                result.addError("Expected " + expectedHeaders.length + " columns, found " + actualHeaders.length);
            }
            
            for (int i = 0; i < Math.min(actualHeaders.length, expectedHeaders.length); i++) {
                if (!expectedHeaders[i].equalsIgnoreCase(actualHeaders[i].trim())) {
                    result.addWarning("Column " + (i + 1) + ": expected '" + expectedHeaders[i] + 
                                    "', found '" + actualHeaders[i] + "'");
                }
            }
            
            // Count data rows
            int rowCount = 0;
            while (reader.readLine() != null) {
                rowCount++;
            }
            result.setRowCount(rowCount);
            
        }
        
        return result;
    }
    
    /**
     * Class to hold CSV validation results.
     */
    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private int rowCount = 0;
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public int getRowCount() {
            return rowCount;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result: ");
            sb.append(isValid() ? "VALID" : "INVALID").append("\n");
            sb.append("Data Rows: ").append(rowCount).append("\n");
            
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}