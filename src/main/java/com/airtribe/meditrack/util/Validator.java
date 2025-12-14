package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.exception.InvalidDataException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Centralized validation utility class.
 * This demonstrates validation patterns and centralized validation logic.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public final class Validator {
    
    // Compiled regex patterns for better performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z\\s'-]{2,50}$"
    );
    
    private static final Pattern ID_PATTERN = Pattern.compile(
        "^[A-Z][0-9]{5}$"
    );
    
    private static final Pattern LICENSE_PATTERN = Pattern.compile(
        "^[A-Z]{2}[0-9]{6}$"
    );
    
    // Private constructor to prevent instantiation
    private Validator() {
        throw new UnsupportedOperationException("Validator is a utility class and cannot be instantiated");
    }
    
    /**
     * Validates if a string is not null or empty.
     * 
     * @param value the value to validate
     * @param fieldName the field name for error reporting
     * @throws InvalidDataException if validation fails
     */
    public static void validateRequired(String value, String fieldName) throws InvalidDataException {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidDataException.forNullOrEmpty(fieldName);
        }
    }
    
    /**
     * Validates string length.
     * 
     * @param value the string to validate
     * @param fieldName the field name
     * @param minLength minimum length
     * @param maxLength maximum length
     * @throws InvalidDataException if validation fails
     */
    public static void validateStringLength(String value, String fieldName, int minLength, int maxLength) 
            throws InvalidDataException {
        if (value != null && (value.length() < minLength || value.length() > maxLength)) {
            throw InvalidDataException.forStringLength(fieldName, value, minLength, maxLength);
        }
    }
    
    /**
     * Validates numeric range.
     * 
     * @param value the number to validate
     * @param fieldName the field name
     * @param min minimum value
     * @param max maximum value
     * @throws InvalidDataException if validation fails
     */
    public static void validateNumericRange(Number value, String fieldName, Number min, Number max) 
            throws InvalidDataException {
        if (value != null && (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue())) {
            throw InvalidDataException.forNumericRange(fieldName, value, min, max);
        }
    }
    
    /**
     * Validates email format.
     * 
     * @param email the email to validate
     * @throws InvalidDataException if email format is invalid
     */
    public static void validateEmail(String email) throws InvalidDataException {
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw InvalidDataException.forInvalidEmail(email);
        }
    }
    
    /**
     * Validates phone number format.
     * 
     * @param phone the phone number to validate
     * @throws InvalidDataException if phone format is invalid
     */
    public static void validatePhone(String phone) throws InvalidDataException {
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.replaceAll("[\\s()-]", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                throw InvalidDataException.forInvalidPhone(phone);
            }
        }
    }
    
    /**
     * Validates name format.
     * 
     * @param name the name to validate
     * @param fieldName the field name
     * @throws InvalidDataException if name format is invalid
     */
    public static void validateName(String name, String fieldName) throws InvalidDataException {
        validateRequired(name, fieldName);
        validateStringLength(name, fieldName, Constants.MIN_NAME_LENGTH, Constants.MAX_NAME_LENGTH);
        
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new InvalidDataException(
                "Field '" + fieldName + "' contains invalid characters",
                fieldName,
                name,
                "Only letters, spaces, apostrophes, and hyphens are allowed"
            );
        }
    }
    
    /**
     * Validates ID format (e.g., P00001, D00001).
     * 
     * @param id the ID to validate
     * @param fieldName the field name
     * @throws InvalidDataException if ID format is invalid
     */
    public static void validateId(String id, String fieldName) throws InvalidDataException {
        validateRequired(id, fieldName);
        
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new InvalidDataException(
                "Field '" + fieldName + "' must be in format: Letter followed by 5 digits (e.g., P00001)",
                fieldName,
                id,
                "Format: [A-Z][0-9]{5}"
            );
        }
    }
    
    /**
     * Validates medical license number format.
     * 
     * @param licenseNumber the license number to validate
     * @throws InvalidDataException if license format is invalid
     */
    public static void validateLicenseNumber(String licenseNumber) throws InvalidDataException {
        validateRequired(licenseNumber, "licenseNumber");
        
        if (!LICENSE_PATTERN.matcher(licenseNumber).matches()) {
            throw new InvalidDataException(
                "License number must be in format: Two letters followed by 6 digits (e.g., MD123456)",
                "licenseNumber",
                licenseNumber,
                "Format: [A-Z]{2}[0-9]{6}"
            );
        }
    }
    
    /**
     * Validates date of birth.
     * 
     * @param dateOfBirth the date of birth to validate
     * @throws InvalidDataException if date is invalid
     */
    public static void validateDateOfBirth(LocalDate dateOfBirth) throws InvalidDataException {
        if (dateOfBirth == null) {
            throw InvalidDataException.forNullOrEmpty("dateOfBirth");
        }
        
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw InvalidDataException.forPastDate("dateOfBirth", dateOfBirth);
        }
        
        int age = java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < Constants.MIN_AGE || age > Constants.MAX_AGE) {
            throw InvalidDataException.forNumericRange("age", age, Constants.MIN_AGE, Constants.MAX_AGE);
        }
    }
    
    /**
     * Validates appointment date and time.
     * 
     * @param appointmentDateTime the appointment date/time to validate
     * @throws InvalidDataException if date/time is invalid
     */
    public static void validateAppointmentDateTime(LocalDateTime appointmentDateTime) throws InvalidDataException {
        if (appointmentDateTime == null) {
            throw InvalidDataException.forNullOrEmpty("appointmentDateTime");
        }
        
        // Check if appointment is too far in the past (more than 1 day)
        if (appointmentDateTime.isBefore(LocalDateTime.now().minusDays(1))) {
            throw new InvalidDataException(
                "Appointment cannot be scheduled more than 1 day in the past",
                "appointmentDateTime",
                appointmentDateTime,
                "Must be within 1 day of current time or in the future"
            );
        }
        
        // Check if appointment is too far in the future (more than 1 year)
        if (appointmentDateTime.isAfter(LocalDateTime.now().plusYears(1))) {
            throw new InvalidDataException(
                "Appointment cannot be scheduled more than 1 year in the future",
                "appointmentDateTime",
                appointmentDateTime,
                "Must be within 1 year from current date"
            );
        }
        
        // Check if appointment is during clinic hours
        int hour = appointmentDateTime.getHour();
        if (hour < Constants.CLINIC_START_HOUR || hour >= Constants.CLINIC_END_HOUR) {
            throw new InvalidDataException(
                "Appointment must be during clinic hours (" + Constants.CLINIC_START_HOUR + 
                ":00 - " + Constants.CLINIC_END_HOUR + ":00)",
                "appointmentDateTime",
                appointmentDateTime,
                "Clinic hours: " + Constants.CLINIC_START_HOUR + ":00 - " + Constants.CLINIC_END_HOUR + ":00"
            );
        }
    }
    
    /**
     * Validates gender value.
     * 
     * @param gender the gender to validate
     * @throws InvalidDataException if gender is invalid
     */
    public static void validateGender(String gender) throws InvalidDataException {
        validateRequired(gender, "gender");
        
        String[] validGenders = {"Male", "Female", "Other", "M", "F", "O"};
        boolean isValid = false;
        
        for (String validGender : validGenders) {
            if (validGender.equalsIgnoreCase(gender.trim())) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw InvalidDataException.forInvalidEnum("gender", gender, validGenders);
        }
    }
    
    /**
     * Validates consultation fee.
     * 
     * @param consultationFee the fee to validate
     * @throws InvalidDataException if fee is invalid
     */
    public static void validateConsultationFee(double consultationFee) throws InvalidDataException {
        if (consultationFee < 0) {
            throw new InvalidDataException(
                "Consultation fee cannot be negative",
                "consultationFee",
                consultationFee,
                "Must be >= 0"
            );
        }
        
        if (consultationFee > 50000) { // Reasonable upper limit
            throw new InvalidDataException(
                "Consultation fee seems unusually high",
                "consultationFee",
                consultationFee,
                "Must be <= 50000"
            );
        }
    }
    
    /**
     * Validates years of experience.
     * 
     * @param experience the years of experience to validate
     * @throws InvalidDataException if experience is invalid
     */
    public static void validateExperience(int experience) throws InvalidDataException {
        if (experience < 0) {
            throw new InvalidDataException(
                "Years of experience cannot be negative",
                "yearsOfExperience",
                experience,
                "Must be >= 0"
            );
        }
        
        if (experience > 60) {
            throw new InvalidDataException(
                "Years of experience seems unusually high",
                "yearsOfExperience",
                experience,
                "Must be <= 60"
            );
        }
    }
    
    /**
     * Validates blood group.
     * 
     * @param bloodGroup the blood group to validate
     * @throws InvalidDataException if blood group is invalid
     */
    public static void validateBloodGroup(String bloodGroup) throws InvalidDataException {
        if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
            String[] validBloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            boolean isValid = false;
            
            for (String validGroup : validBloodGroups) {
                if (validGroup.equalsIgnoreCase(bloodGroup.trim())) {
                    isValid = true;
                    break;
                }
            }
            
            if (!isValid) {
                throw InvalidDataException.forInvalidEnum("bloodGroup", bloodGroup, validBloodGroups);
            }
        }
    }
    
    /**
     * Validates patient type.
     * 
     * @param patientType the patient type to validate
     * @throws InvalidDataException if patient type is invalid
     */
    public static void validatePatientType(String patientType) throws InvalidDataException {
        validateRequired(patientType, "patientType");
        
        String[] validTypes = {"INPATIENT", "OUTPATIENT", "EMERGENCY"};
        boolean isValid = false;
        
        for (String validType : validTypes) {
            if (validType.equalsIgnoreCase(patientType.trim())) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw InvalidDataException.forInvalidEnum("patientType", patientType, validTypes);
        }
    }
    
    /**
     * Validates appointment duration.
     * 
     * @param durationMinutes the duration in minutes to validate
     * @throws InvalidDataException if duration is invalid
     */
    public static void validateAppointmentDuration(int durationMinutes) throws InvalidDataException {
        if (durationMinutes <= 0) {
            throw new InvalidDataException(
                "Appointment duration must be positive",
                "durationMinutes",
                durationMinutes,
                "Must be > 0"
            );
        }
        
        if (durationMinutes > 480) { // Max 8 hours
            throw new InvalidDataException(
                "Appointment duration cannot exceed 8 hours",
                "durationMinutes",
                durationMinutes,
                "Must be <= 480 minutes (8 hours)"
            );
        }
        
        // Duration should be in 15-minute increments
        if (durationMinutes % 15 != 0) {
            throw new InvalidDataException(
                "Appointment duration should be in 15-minute increments",
                "durationMinutes",
                durationMinutes,
                "Must be multiple of 15 minutes"
            );
        }
    }
    
    /**
     * Validates a collection is not null or empty.
     * 
     * @param collection the collection to validate
     * @param fieldName the field name
     * @throws InvalidDataException if collection is null or empty
     */
    public static void validateNotEmpty(List<?> collection, String fieldName) throws InvalidDataException {
        if (collection == null || collection.isEmpty()) {
            throw new InvalidDataException(
                "Field '" + fieldName + "' cannot be null or empty",
                fieldName,
                collection,
                "Must contain at least one item"
            );
        }
    }
    
    /**
     * Validates that a value is not null.
     * 
     * @param value the value to validate
     * @param fieldName the field name
     * @throws InvalidDataException if value is null
     */
    public static void validateNotNull(Object value, String fieldName) throws InvalidDataException {
        if (value == null) {
            throw InvalidDataException.forNullOrEmpty(fieldName);
        }
    }
    
    /**
     * Performs comprehensive validation for a Person entity.
     * 
     * @param firstName the first name
     * @param lastName the last name
     * @param dateOfBirth the date of birth
     * @param gender the gender
     * @param email the email (optional)
     * @param phone the phone (optional)
     * @return list of validation errors (empty if valid)
     */
    public static List<String> validatePerson(String firstName, String lastName, LocalDate dateOfBirth,
                                            String gender, String email, String phone) {
        List<String> errors = new ArrayList<>();
        
        try {
            validateName(firstName, "firstName");
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validateName(lastName, "lastName");
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validateDateOfBirth(dateOfBirth);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validateGender(gender);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validateEmail(email);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validatePhone(phone);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Performs comprehensive validation for a Doctor entity.
     * 
     * @param firstName the first name
     * @param lastName the last name
     * @param dateOfBirth the date of birth
     * @param gender the gender
     * @param email the email
     * @param phone the phone
     * @param licenseNumber the medical license number
     * @param yearsOfExperience the years of experience
     * @param consultationFee the consultation fee
     * @return list of validation errors (empty if valid)
     */
    public static List<String> validateDoctor(String firstName, String lastName, LocalDate dateOfBirth,
                                            String gender, String email, String phone, String licenseNumber,
                                            int yearsOfExperience, double consultationFee) {
        List<String> errors = validatePerson(firstName, lastName, dateOfBirth, gender, email, phone);
        
        try {
            validateLicenseNumber(licenseNumber);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validateExperience(yearsOfExperience);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        try {
            validateConsultationFee(consultationFee);
        } catch (InvalidDataException e) {
            errors.add(e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Quick validation method that returns boolean result.
     * 
     * @param value the value to validate
     * @param validationFunction the validation function to apply
     * @return true if valid, false otherwise
     */
    public static boolean isValid(Object value, ValidationFunction validationFunction) {
        try {
            validationFunction.validate(value);
            return true;
        } catch (InvalidDataException e) {
            return false;
        }
    }
    
    /**
     * Functional interface for validation functions.
     */
    @FunctionalInterface
    public interface ValidationFunction {
        void validate(Object value) throws InvalidDataException;
    }
    
    /**
     * Validates multiple fields and collects all errors.
     * 
     * @param validations array of validation operations
     * @return list of all validation errors
     */
    public static List<String> validateAll(ValidationOperation... validations) {
        List<String> errors = new ArrayList<>();
        
        for (ValidationOperation validation : validations) {
            try {
                validation.validate();
            } catch (InvalidDataException e) {
                errors.add(e.getMessage());
            }
        }
        
        return errors;
    }
    
    /**
     * Functional interface for validation operations.
     */
    @FunctionalInterface
    public interface ValidationOperation {
        void validate() throws InvalidDataException;
    }
    
    /**
     * Validates and throws exception if any validation fails.
     * 
     * @param validations array of validation operations
     * @throws InvalidDataException if any validation fails
     */
    public static void validateAllAndThrow(ValidationOperation... validations) throws InvalidDataException {
        List<String> errors = validateAll(validations);
        
        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new InvalidDataException("Multiple validation errors: " + combinedErrors);
        }
    }
}