package com.airtribe.meditrack.entity;

/**
 * Enum representing different appointment statuses.
 * This demonstrates enum usage with state transitions and validation.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public enum AppointmentStatus {
    
    PENDING("Pending", "Appointment is awaiting confirmation", false),
    CONFIRMED("Confirmed", "Appointment has been confirmed", false),
    COMPLETED("Completed", "Appointment has been completed", true),
    CANCELLED("Cancelled", "Appointment has been cancelled", true),
    NO_SHOW("No Show", "Patient did not show up for the appointment", true),
    RESCHEDULED("Rescheduled", "Appointment has been rescheduled", false),
    IN_PROGRESS("In Progress", "Appointment is currently in progress", false);
    
    private final String displayName;
    private final String description;
    private final boolean isFinal;
    
    /**
     * Constructor for AppointmentStatus enum.
     * 
     * @param displayName The display name of the status
     * @param description Description of the status
     * @param isFinal Whether this is a final status (cannot be changed)
     */
    AppointmentStatus(String displayName, String description, boolean isFinal) {
        this.displayName = displayName;
        this.description = description;
        this.isFinal = isFinal;
    }
    
    /**
     * Gets the display name of the status.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the status.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status is final (cannot be changed).
     * 
     * @return true if final, false otherwise
     */
    public boolean isFinal() {
        return isFinal;
    }
    
    /**
     * Checks if transition to another status is allowed.
     * 
     * @param newStatus The status to transition to
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransitionTo(AppointmentStatus newStatus) {
        if (this.isFinal) {
            return false; // Cannot transition from final states
        }
        
        // Define valid transitions
        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED || newStatus == CANCELLED || newStatus == RESCHEDULED;
            case CONFIRMED:
                return newStatus == IN_PROGRESS || newStatus == COMPLETED || 
                       newStatus == CANCELLED || newStatus == NO_SHOW || newStatus == RESCHEDULED;
            case RESCHEDULED:
                return newStatus == CONFIRMED || newStatus == CANCELLED;
            case IN_PROGRESS:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            default:
                return false;
        }
    }
    
    /**
     * Get all valid transition statuses from current status.
     * 
     * @return Array of valid transition statuses
     */
    public AppointmentStatus[] getValidTransitions() {
        if (this.isFinal) {
            return new AppointmentStatus[0];
        }
        
        return java.util.Arrays.stream(values())
                .filter(this::canTransitionTo)
                .toArray(AppointmentStatus[]::new);
    }
    
    /**
     * Find status by display name (case-insensitive).
     * 
     * @param name The name to search for
     * @return The matching status or null if not found
     */
    public static AppointmentStatus findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        for (AppointmentStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(name.trim()) || 
                status.name().equalsIgnoreCase(name.trim())) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * Get all active (non-final) statuses.
     * 
     * @return Array of active statuses
     */
    public static AppointmentStatus[] getActiveStatuses() {
        return java.util.Arrays.stream(values())
                .filter(status -> !status.isFinal)
                .toArray(AppointmentStatus[]::new);
    }
    
    /**
     * Get all final statuses.
     * 
     * @return Array of final statuses
     */
    public static AppointmentStatus[] getFinalStatuses() {
        return java.util.Arrays.stream(values())
                .filter(status -> status.isFinal)
                .toArray(AppointmentStatus[]::new);
    }
    
    /**
     * Check if the status represents a billable appointment.
     * 
     * @return true if billable, false otherwise
     */
    public boolean isBillable() {
        return this == COMPLETED;
    }
    
    /**
     * Check if the status allows modifications to appointment details.
     * 
     * @return true if modifications are allowed, false otherwise
     */
    public boolean allowsModification() {
        return this == PENDING || this == CONFIRMED;
    }
    
    /**
     * Get status color for UI representation.
     * 
     * @return Color code or name for UI
     */
    public String getStatusColor() {
        switch (this) {
            case PENDING:
                return "YELLOW";
            case CONFIRMED:
                return "BLUE";
            case COMPLETED:
                return "GREEN";
            case CANCELLED:
                return "RED";
            case NO_SHOW:
                return "ORANGE";
            case RESCHEDULED:
                return "PURPLE";
            case IN_PROGRESS:
                return "CYAN";
            default:
                return "GRAY";
        }
    }
    
    /**
     * Get status priority for sorting (higher number = higher priority).
     * 
     * @return Priority value
     */
    public int getPriority() {
        switch (this) {
            case IN_PROGRESS:
                return 5;
            case CONFIRMED:
                return 4;
            case PENDING:
                return 3;
            case RESCHEDULED:
                return 2;
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                return 1;
            default:
                return 0;
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Get detailed string representation.
     * 
     * @return Detailed string with status info
     */
    public String toDetailedString() {
        return String.format("%s - %s %s", 
                displayName, 
                description, 
                isFinal ? "(Final)" : "(Active)");
    }
}