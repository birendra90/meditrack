package com.airtribe.meditrack.exception;

/**
 * Custom exception thrown when invalid data is encountered.
 * This demonstrates custom exception creation with validation context.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class InvalidDataException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String fieldName;
    private final Object invalidValue;
    private final String validationRule;
    
    /**
     * Constructs a new InvalidDataException with the specified detail message.
     * 
     * @param message the detail message
     */
    public InvalidDataException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
        this.validationRule = null;
    }
    
    /**
     * Constructs a new InvalidDataException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.invalidValue = null;
        this.validationRule = null;
    }
    
    /**
     * Constructs a new InvalidDataException with field information.
     * 
     * @param message the detail message
     * @param fieldName the name of the field with invalid data
     * @param invalidValue the invalid value that was provided
     */
    public InvalidDataException(String message, String fieldName, Object invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.validationRule = null;
    }
    
    /**
     * Constructs a new InvalidDataException with complete validation context.
     * 
     * @param message the detail message
     * @param fieldName the name of the field with invalid data
     * @param invalidValue the invalid value that was provided
     * @param validationRule the validation rule that was violated
     */
    public InvalidDataException(String message, String fieldName, Object invalidValue, String validationRule) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.validationRule = validationRule;
    }
    
    /**
     * Constructs a new InvalidDataException with complete validation context and cause.
     * 
     * @param message the detail message
     * @param fieldName the name of the field with invalid data
     * @param invalidValue the invalid value that was provided
     * @param validationRule the validation rule that was violated
     * @param cause the cause of this exception
     */
    public InvalidDataException(String message, String fieldName, Object invalidValue, String validationRule, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.validationRule = validationRule;
    }
    
    /**
     * Gets the name of the field with invalid data.
     * 
     * @return the field name, or null if not specified
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Gets the invalid value that was provided.
     * 
     * @return the invalid value, or null if not specified
     */
    public Object getInvalidValue() {
        return invalidValue;
    }
    
    /**
     * Gets the validation rule that was violated.
     * 
     * @return the validation rule, or null if not specified
     */
    public String getValidationRule() {
        return validationRule;
    }
    
    /**
     * Creates a detailed error message including all available validation context.
     * 
     * @return detailed error message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (fieldName != null) {
            sb.append(" [Field: ").append(fieldName).append("]");
        }
        
        if (invalidValue != null) {
            sb.append(" [Invalid Value: ").append(invalidValue).append("]");
        }
        
        if (validationRule != null) {
            sb.append(" [Rule: ").append(validationRule).append("]");
        }
        
        if (getCause() != null) {
            sb.append(" [Caused by: ").append(getCause().getMessage()).append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Factory method for null or empty string validation errors.
     * 
     * @param fieldName the field name
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forNullOrEmpty(String fieldName) {
        return new InvalidDataException(
                "Field '" + fieldName + "' cannot be null or empty",
                fieldName,
                null,
                "Required field"
        );
    }
    
    /**
     * Factory method for numeric range validation errors.
     * 
     * @param fieldName the field name
     * @param value the invalid value
     * @param min minimum allowed value
     * @param max maximum allowed value
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forNumericRange(String fieldName, Number value, Number min, Number max) {
        return new InvalidDataException(
                "Field '" + fieldName + "' must be between " + min + " and " + max,
                fieldName,
                value,
                "Range: " + min + "-" + max
        );
    }
    
    /**
     * Factory method for string length validation errors.
     * 
     * @param fieldName the field name
     * @param value the invalid value
     * @param minLength minimum allowed length
     * @param maxLength maximum allowed length
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forStringLength(String fieldName, String value, int minLength, int maxLength) {
        return new InvalidDataException(
                "Field '" + fieldName + "' length must be between " + minLength + " and " + maxLength + " characters",
                fieldName,
                value,
                "Length: " + minLength + "-" + maxLength + " chars"
        );
    }
    
    /**
     * Factory method for pattern validation errors.
     * 
     * @param fieldName the field name
     * @param value the invalid value
     * @param pattern the pattern that should be matched
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forPattern(String fieldName, String value, String pattern) {
        return new InvalidDataException(
                "Field '" + fieldName + "' does not match required pattern",
                fieldName,
                value,
                "Pattern: " + pattern
        );
    }
    
    /**
     * Factory method for email validation errors.
     * 
     * @param email the invalid email
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forInvalidEmail(String email) {
        return new InvalidDataException(
                "Invalid email format",
                "email",
                email,
                "Valid email format required"
        );
    }
    
    /**
     * Factory method for phone number validation errors.
     * 
     * @param phone the invalid phone number
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forInvalidPhone(String phone) {
        return new InvalidDataException(
                "Invalid phone number format",
                "phone",
                phone,
                "Valid phone number required (10-15 digits)"
        );
    }
    
    /**
     * Factory method for date validation errors.
     * 
     * @param fieldName the field name
     * @param dateString the invalid date string
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forInvalidDate(String fieldName, String dateString) {
        return new InvalidDataException(
                "Invalid date format for field '" + fieldName + "'",
                fieldName,
                dateString,
                "Date format: dd/MM/yyyy"
        );
    }
    
    /**
     * Factory method for future date validation errors.
     * 
     * @param fieldName the field name
     * @param date the invalid date
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forPastDate(String fieldName, Object date) {
        return new InvalidDataException(
                "Field '" + fieldName + "' cannot be in the past",
                fieldName,
                date,
                "Future date required"
        );
    }
    
    /**
     * Factory method for enum validation errors.
     * 
     * @param fieldName the field name
     * @param value the invalid value
     * @param validValues the valid enum values
     * @return new InvalidDataException instance
     */
    public static InvalidDataException forInvalidEnum(String fieldName, String value, String[] validValues) {
        return new InvalidDataException(
                "Invalid value for field '" + fieldName + "'",
                fieldName,
                value,
                "Valid values: " + String.join(", ", validValues)
        );
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getDetailedMessage();
    }
}