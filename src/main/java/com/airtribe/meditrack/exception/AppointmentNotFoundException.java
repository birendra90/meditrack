package com.airtribe.meditrack.exception;

/**
 * Custom exception thrown when an appointment is not found.
 * This demonstrates custom exception creation with chaining support.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class AppointmentNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String appointmentId;
    private final String searchCriteria;
    
    /**
     * Constructs a new AppointmentNotFoundException with the specified detail message.
     * 
     * @param message the detail message
     */
    public AppointmentNotFoundException(String message) {
        super(message);
        this.appointmentId = null;
        this.searchCriteria = null;
    }
    
    /**
     * Constructs a new AppointmentNotFoundException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public AppointmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.appointmentId = null;
        this.searchCriteria = null;
    }
    
    /**
     * Constructs a new AppointmentNotFoundException with appointment ID.
     * 
     * @param message the detail message
     * @param appointmentId the ID of the appointment that was not found
     */
    public AppointmentNotFoundException(String message, String appointmentId) {
        super(message);
        this.appointmentId = appointmentId;
        this.searchCriteria = null;
    }
    
    /**
     * Constructs a new AppointmentNotFoundException with appointment ID and search criteria.
     * 
     * @param message the detail message
     * @param appointmentId the ID of the appointment that was not found
     * @param searchCriteria the search criteria used
     */
    public AppointmentNotFoundException(String message, String appointmentId, String searchCriteria) {
        super(message);
        this.appointmentId = appointmentId;
        this.searchCriteria = searchCriteria;
    }
    
    /**
     * Constructs a new AppointmentNotFoundException with appointment ID, search criteria, and cause.
     * 
     * @param message the detail message
     * @param appointmentId the ID of the appointment that was not found
     * @param searchCriteria the search criteria used
     * @param cause the cause of this exception
     */
    public AppointmentNotFoundException(String message, String appointmentId, String searchCriteria, Throwable cause) {
        super(message, cause);
        this.appointmentId = appointmentId;
        this.searchCriteria = searchCriteria;
    }
    
    /**
     * Gets the appointment ID that was not found.
     * 
     * @return the appointment ID, or null if not specified
     */
    public String getAppointmentId() {
        return appointmentId;
    }
    
    /**
     * Gets the search criteria that was used.
     * 
     * @return the search criteria, or null if not specified
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }
    
    /**
     * Creates a detailed error message including all available information.
     * 
     * @return detailed error message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (appointmentId != null) {
            sb.append(" [Appointment ID: ").append(appointmentId).append("]");
        }
        
        if (searchCriteria != null) {
            sb.append(" [Search Criteria: ").append(searchCriteria).append("]");
        }
        
        if (getCause() != null) {
            sb.append(" [Caused by: ").append(getCause().getMessage()).append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Factory method to create exception for appointment ID not found.
     * 
     * @param appointmentId the appointment ID that was not found
     * @return new AppointmentNotFoundException instance
     */
    public static AppointmentNotFoundException forAppointmentId(String appointmentId) {
        return new AppointmentNotFoundException(
                "Appointment with ID '" + appointmentId + "' not found",
                appointmentId
        );
    }
    
    /**
     * Factory method to create exception for search criteria not yielding results.
     * 
     * @param searchCriteria the search criteria used
     * @return new AppointmentNotFoundException instance
     */
    public static AppointmentNotFoundException forSearchCriteria(String searchCriteria) {
        return new AppointmentNotFoundException(
                "No appointments found matching criteria: " + searchCriteria,
                null,
                searchCriteria
        );
    }
    
    /**
     * Factory method to create exception for patient having no appointments.
     * 
     * @param patientId the patient ID
     * @return new AppointmentNotFoundException instance
     */
    public static AppointmentNotFoundException forPatient(String patientId) {
        return new AppointmentNotFoundException(
                "No appointments found for patient with ID: " + patientId,
                null,
                "Patient ID: " + patientId
        );
    }
    
    /**
     * Factory method to create exception for doctor having no appointments.
     * 
     * @param doctorId the doctor ID
     * @return new AppointmentNotFoundException instance
     */
    public static AppointmentNotFoundException forDoctor(String doctorId) {
        return new AppointmentNotFoundException(
                "No appointments found for doctor with ID: " + doctorId,
                null,
                "Doctor ID: " + doctorId
        );
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getDetailedMessage();
    }
}