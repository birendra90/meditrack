package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.interfaces.Searchable;
import com.airtribe.meditrack.constants.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Appointment entity representing medical appointments in the system.
 * This demonstrates inheritance, polymorphism, cloning, and enum usage.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class Appointment extends MedicalEntity implements Searchable<Appointment>, Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    // Appointment-specific fields
    private String patientId;
    private String doctorId;
    private LocalDateTime appointmentDateTime;
    private int durationMinutes;
    private AppointmentStatus status;
    private String reasonForVisit;
    private String notes;
    private String symptoms;
    private String diagnosis;
    private String prescription;
    private double consultationFee;
    private boolean isEmergency;
    private String appointmentType; // CONSULTATION, FOLLOW_UP, CHECKUP, SURGERY
    private LocalDateTime reminderSent;
    private int rescheduleCount;
    private String cancellationReason;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    
    // References to related entities (for easy access)
    private transient Patient patient;  // transient so it's not serialized
    private transient Doctor doctor;    // transient so it's not serialized
    
    // Static counter for tracking total appointments
    private static int totalAppointments = 0;
    
    // Static initialization block
    static {
        System.out.println("[STATIC BLOCK] Appointment class initialized");
    }
    
    /**
     * Default constructor.
     */
    public Appointment() {
        super();
        initializeAppointment();
    }
    
    /**
     * Simple constructor with required fields.
     */
    public Appointment(String id, String patientId, String doctorId,
                      LocalDateTime appointmentDateTime, String reasonForVisit) {
        super(id);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.reasonForVisit = reasonForVisit;
        initializeAppointment();
    }
    
    /**
     * Constructor with all fields for service classes.
     */
    public Appointment(String id, String patientId, String doctorId,
                      LocalDateTime appointmentDateTime, int durationMinutes,
                      String reasonForVisit, String appointmentType,
                      double consultationFee, boolean isEmergency) {
        super(id);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.durationMinutes = durationMinutes;
        this.reasonForVisit = reasonForVisit;
        this.appointmentType = appointmentType;
        this.consultationFee = consultationFee;
        this.isEmergency = isEmergency;
        initializeAppointment();
    }
    
    /**
     * Copy constructor for cloning.
     */
    private Appointment(Appointment original) {
        super(original.getId());
        copyFromOriginal(original);
    }
    
    /**
     * Initializes appointment-specific defaults.
     */
    private void initializeAppointment() {
        this.durationMinutes = Constants.DEFAULT_APPOINTMENT_DURATION;
        this.status = AppointmentStatus.PENDING;
        this.appointmentType = "CONSULTATION";
        this.rescheduleCount = 0;
        this.isEmergency = false;
        
        synchronized (Appointment.class) {
            totalAppointments++;
        }
    }
    
    /**
     * Helper method for copying data from original appointment.
     */
    private void copyFromOriginal(Appointment original) {
        this.patientId = original.patientId;
        this.doctorId = original.doctorId;
        this.appointmentDateTime = original.appointmentDateTime;
        this.durationMinutes = original.durationMinutes;
        this.status = original.status;
        this.reasonForVisit = original.reasonForVisit;
        this.notes = original.notes;
        this.symptoms = original.symptoms;
        this.diagnosis = original.diagnosis;
        this.prescription = original.prescription;
        this.consultationFee = original.consultationFee;
        this.isEmergency = original.isEmergency;
        this.appointmentType = original.appointmentType;
        this.reminderSent = original.reminderSent;
        this.rescheduleCount = original.rescheduleCount;
        this.cancellationReason = original.cancellationReason;
        this.actualStartTime = original.actualStartTime;
        this.actualEndTime = original.actualEndTime;
        
        // Note: We don't copy the transient patient/doctor references
        // They should be resolved separately if needed
    }
    
    // Getters and Setters
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
        updateTimestamp();
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
        updateTimestamp();
    }
    
    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }
    
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
        updateTimestamp();
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
        updateTimestamp();
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus newStatus) {
        if (this.status != null && !this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + this.status + " to " + newStatus
            );
        }
        this.status = newStatus;
        updateTimestamp();
    }
    
    public String getReasonForVisit() {
        return reasonForVisit;
    }
    
    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
        updateTimestamp();
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
        updateTimestamp();
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
        updateTimestamp();
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
        updateTimestamp();
    }
    
    public String getPrescription() {
        return prescription;
    }
    
    public void setPrescription(String prescription) {
        this.prescription = prescription;
        updateTimestamp();
    }
    
    public double getConsultationFee() {
        return consultationFee;
    }
    
    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        updateTimestamp();
    }
    
    public boolean isEmergency() {
        return isEmergency;
    }
    
    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
        updateTimestamp();
    }
    
    public String getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
        updateTimestamp();
    }
    
    public LocalDateTime getReminderSent() {
        return reminderSent;
    }
    
    public void setReminderSent(LocalDateTime reminderSent) {
        this.reminderSent = reminderSent;
        updateTimestamp();
    }
    
    public int getRescheduleCount() {
        return rescheduleCount;
    }
    
    public void incrementRescheduleCount() {
        this.rescheduleCount++;
        updateTimestamp();
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
        updateTimestamp();
    }
    
    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }
    
    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
        updateTimestamp();
    }
    
    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }
    
    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
        updateTimestamp();
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getId();
        }
    }
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getId();
            this.consultationFee = doctor.getEffectiveConsultationFee(isEmergency);
        }
    }
    
    // Static method to get total appointments count
    public static int getTotalAppointments() {
        return totalAppointments;
    }
    
    // Cloneable implementation demonstrating deep copy
    
    @Override
    public Appointment clone() throws CloneNotSupportedException {
        Appointment cloned = (Appointment) super.clone();
        
        // Deep copy of mutable date objects
        if (this.appointmentDateTime != null) {
            cloned.appointmentDateTime = LocalDateTime.from(this.appointmentDateTime);
        }
        if (this.reminderSent != null) {
            cloned.reminderSent = LocalDateTime.from(this.reminderSent);
        }
        if (this.actualStartTime != null) {
            cloned.actualStartTime = LocalDateTime.from(this.actualStartTime);
        }
        if (this.actualEndTime != null) {
            cloned.actualEndTime = LocalDateTime.from(this.actualEndTime);
        }
        
        // Note: Enum values (AppointmentStatus) are immutable, so shallow copy is fine
        // Note: Strings are immutable, so shallow copy is fine
        // Note: Transient fields (patient, doctor) are not copied
        
        return cloned;
    }
    
    /**
     * Creates a custom deep copy using copy constructor.
     */
    public Appointment deepCopy() {
        return new Appointment(this);
    }
    
    // Implementation of Searchable interface
    
    @Override
    public String getSearchId() {
        return getId();
    }
    
    @Override
    public String getPrimarySearchTerm() {
        return getId() + " - " + reasonForVisit;
    }
    
    @Override
    public List<String> getSearchableTerms() {
        List<String> terms = new ArrayList<>();
        
        if (getId() != null) terms.add(getId());
        if (patientId != null) terms.add(patientId);
        if (doctorId != null) terms.add(doctorId);
        if (reasonForVisit != null) terms.add(reasonForVisit);
        if (symptoms != null) terms.add(symptoms);
        if (diagnosis != null) terms.add(diagnosis);
        if (appointmentType != null) terms.add(appointmentType);
        if (status != null) terms.add(status.getDisplayName());
        
        // Add formatted date for searching
        if (appointmentDateTime != null) {
            terms.add(appointmentDateTime.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)));
            terms.add(appointmentDateTime.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
        
        return terms;
    }
    
    @Override
    public boolean matches(String criteria, boolean caseSensitive) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return false;
        }
        
        // Exact ID match
        if (getId() != null && getId().equals(criteria)) {
            return true;
        }
        
        // Patient ID or Doctor ID match
        if ((patientId != null && patientId.equals(criteria)) || 
            (doctorId != null && doctorId.equals(criteria))) {
            return true;
        }
        
        // Use the default implementation from interface
        return matchesAny(criteria, caseSensitive);
    }
    
    // Validation implementation
    
    @Override
    public boolean isValid() {
        return getValidationErrors().length == 0;
    }
    
    @Override
    public String[] getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        // Required field validation
        if (patientId == null || patientId.trim().isEmpty()) {
            errors.add("Patient ID is required");
        }
        
        if (doctorId == null || doctorId.trim().isEmpty()) {
            errors.add("Doctor ID is required");
        }
        
        if (appointmentDateTime == null) {
            errors.add("Appointment date and time is required");
        } else {
            // Check if appointment is in the past (except for completed appointments)
            if (appointmentDateTime.isBefore(LocalDateTime.now()) && 
                status != AppointmentStatus.COMPLETED && status != AppointmentStatus.CANCELLED) {
                errors.add("Appointment cannot be scheduled in the past");
            }
            
            // Check if appointment is during clinic hours
            int hour = appointmentDateTime.getHour();
            if (hour < Constants.CLINIC_START_HOUR || hour >= Constants.CLINIC_END_HOUR) {
                errors.add("Appointment must be during clinic hours (" + 
                          Constants.CLINIC_START_HOUR + ":00 - " + Constants.CLINIC_END_HOUR + ":00)");
            }
        }
        
        if (reasonForVisit == null || reasonForVisit.trim().isEmpty()) {
            errors.add("Reason for visit is required");
        }
        
        if (durationMinutes <= 0 || durationMinutes > 480) { // Max 8 hours
            errors.add("Duration must be between 1 and 480 minutes");
        }
        
        if (consultationFee < 0) {
            errors.add("Consultation fee cannot be negative");
        }
        
        if (appointmentType == null || appointmentType.trim().isEmpty()) {
            errors.add("Appointment type is required");
        } else {
            String[] validTypes = {"CONSULTATION", "FOLLOW_UP", "CHECKUP", "SURGERY", "EMERGENCY"};
            boolean validType = Arrays.asList(validTypes).contains(appointmentType.toUpperCase());
            if (!validType) {
                errors.add("Appointment type must be one of: " + String.join(", ", validTypes));
            }
        }
        
        // Status-specific validations
        if (status == AppointmentStatus.COMPLETED) {
            if (actualStartTime == null || actualEndTime == null) {
                errors.add("Actual start and end times are required for completed appointments");
            }
            if (actualStartTime != null && actualEndTime != null && 
                actualStartTime.isAfter(actualEndTime)) {
                errors.add("Actual start time cannot be after actual end time");
            }
        }
        
        if (status == AppointmentStatus.CANCELLED && 
            (cancellationReason == null || cancellationReason.trim().isEmpty())) {
            errors.add("Cancellation reason is required for cancelled appointments");
        }
        
        return errors.toArray(new String[0]);
    }
    
    @Override
    public String getEntityType() {
        return "Appointment";
    }
    
    @Override
    public String getDisplayName() {
        return String.format("Appointment %s (%s)", getId(), 
                           appointmentDateTime != null ? 
                           appointmentDateTime.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT)) : 
                           "No Date");
    }
    
    // Appointment-specific methods demonstrating business logic
    
    /**
     * Reschedules the appointment to a new date/time.
     * Demonstrates method with business logic and validation.
     * 
     * @param newDateTime the new appointment date/time
     * @param reason the reason for rescheduling
     * @throws IllegalStateException if rescheduling is not allowed
     */
    public void reschedule(LocalDateTime newDateTime, String reason) {
        if (!status.allowsModification()) {
            throw new IllegalStateException("Cannot reschedule appointment with status: " + status);
        }
        
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot reschedule to a past date/time");
        }
        
        this.appointmentDateTime = newDateTime;
        this.status = AppointmentStatus.RESCHEDULED;
        this.rescheduleCount++;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                        "Rescheduled: " + reason + " (" + LocalDateTime.now() + ")";
        }
        updateTimestamp();
    }
    
    /**
     * Confirms the appointment.
     * 
     * @throws IllegalStateException if confirmation is not allowed
     */
    public void confirm() {
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.RESCHEDULED) {
            throw new IllegalStateException("Can only confirm pending or rescheduled appointments");
        }
        setStatus(AppointmentStatus.CONFIRMED);
    }
    
    /**
     * Starts the appointment (marks as in progress).
     * 
     * @throws IllegalStateException if starting is not allowed
     */
    public void start() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Can only start confirmed appointments");
        }
        setStatus(AppointmentStatus.IN_PROGRESS);
        setActualStartTime(LocalDateTime.now());
    }
    
    /**
     * Completes the appointment with diagnosis and prescription.
     * 
     * @param diagnosis the diagnosis
     * @param prescription the prescription (optional)
     * @param notes additional notes (optional)
     */
    public void complete(String diagnosis, String prescription, String notes) {
        if (status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete appointments that are in progress");
        }
        
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        if (notes != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + notes;
        }
        
        setStatus(AppointmentStatus.COMPLETED);
        setActualEndTime(LocalDateTime.now());
    }
    
    /**
     * Cancels the appointment with a reason.
     * 
     * @param reason the cancellation reason
     */
    public void cancel(String reason) {
        if (status.isFinal()) {
            throw new IllegalStateException("Cannot cancel appointment with final status: " + status);
        }
        
        this.cancellationReason = reason;
        setStatus(AppointmentStatus.CANCELLED);
    }
    
    /**
     * Marks patient as no-show.
     */
    public void markNoShow() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Can only mark confirmed appointments as no-show");
        }
        setStatus(AppointmentStatus.NO_SHOW);
    }
    
    /**
     * Checks if appointment is today.
     * 
     * @return true if appointment is today
     */
    public boolean isToday() {
        if (appointmentDateTime == null) return false;
        return appointmentDateTime.toLocalDate().equals(java.time.LocalDate.now());
    }
    
    /**
     * Checks if appointment is overdue (past scheduled time and not completed).
     * 
     * @return true if overdue
     */
    public boolean isOverdue() {
        if (appointmentDateTime == null) return false;
        return appointmentDateTime.isBefore(LocalDateTime.now()) && 
               !status.isFinal() && status != AppointmentStatus.IN_PROGRESS;
    }
    
    /**
     * Gets the end time of the appointment (scheduled).
     * 
     * @return scheduled end time
     */
    public LocalDateTime getScheduledEndTime() {
        if (appointmentDateTime == null) return null;
        return appointmentDateTime.plusMinutes(durationMinutes);
    }
    
    /**
     * Gets actual duration of the appointment.
     * 
     * @return actual duration in minutes, or 0 if not completed
     */
    public long getActualDurationMinutes() {
        if (actualStartTime != null && actualEndTime != null) {
            return java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
        }
        return 0;
    }
    
    /**
     * Checks if appointment needs reminder.
     * 
     * @return true if reminder should be sent
     */
    public boolean needsReminder() {
        if (appointmentDateTime == null || reminderSent != null || status.isFinal()) {
            return false;
        }
        
        // Send reminder 24 hours before appointment
        LocalDateTime reminderTime = appointmentDateTime.minusHours(24);
        return LocalDateTime.now().isAfter(reminderTime);
    }
    
    /**
     * Marks reminder as sent.
     */
    public void markReminderSent() {
        this.reminderSent = LocalDateTime.now();
        updateTimestamp();
    }
    
    /**
     * Gets appointment priority based on type and emergency status.
     * 
     * @return priority level (1-5, where 1 is highest)
     */
    public int getPriority() {
        if (isEmergency) return 1;
        
        switch (appointmentType.toUpperCase()) {
            case "EMERGENCY":
                return 1;
            case "SURGERY":
                return 2;
            case "FOLLOW_UP":
                return 3;
            case "CONSULTATION":
                return 4;
            case "CHECKUP":
                return 5;
            default:
                return 4;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Appointment{id='%s', patient='%s', doctor='%s', dateTime='%s', status='%s'}", 
                getId(), patientId, doctorId, 
                appointmentDateTime != null ? appointmentDateTime.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT)) : "N/A",
                status != null ? status.getDisplayName() : "N/A");
    }
    
    /**
     * Gets detailed appointment information for display.
     * 
     * @return detailed appointment information
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=".repeat(50)).append("\n");
        info.append("APPOINTMENT DETAILS").append("\n");
        info.append("=".repeat(50)).append("\n");
        info.append("ID: ").append(getId()).append("\n");
        info.append("Patient ID: ").append(patientId).append("\n");
        info.append("Doctor ID: ").append(doctorId).append("\n");
        info.append("Date & Time: ").append(appointmentDateTime != null ? 
                   appointmentDateTime.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT)) : "N/A").append("\n");
        info.append("Duration: ").append(durationMinutes).append(" minutes\n");
        info.append("Type: ").append(appointmentType).append("\n");
        info.append("Status: ").append(status != null ? status.getDisplayName() : "N/A").append("\n");
        info.append("Priority: ").append(getPriority()).append(isEmergency ? " (EMERGENCY)" : "").append("\n");
        info.append("Consultation Fee: ₹").append(String.format("%.2f", consultationFee)).append("\n");
        info.append("Reason: ").append(reasonForVisit != null ? reasonForVisit : "N/A").append("\n");
        
        if (symptoms != null) {
            info.append("Symptoms: ").append(symptoms).append("\n");
        }
        
        if (diagnosis != null) {
            info.append("Diagnosis: ").append(diagnosis).append("\n");
        }
        
        if (prescription != null) {
            info.append("Prescription: ").append(prescription).append("\n");
        }
        
        if (notes != null) {
            info.append("Notes: ").append(notes).append("\n");
        }
        
        if (rescheduleCount > 0) {
            info.append("Reschedule Count: ").append(rescheduleCount).append("\n");
        }
        
        if (status == AppointmentStatus.CANCELLED && cancellationReason != null) {
            info.append("Cancellation Reason: ").append(cancellationReason).append("\n");
        }
        
        if (actualStartTime != null) {
            info.append("Actual Start: ").append(actualStartTime.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT))).append("\n");
        }
        
        if (actualEndTime != null) {
            info.append("Actual End: ").append(actualEndTime.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT))).append("\n");
            info.append("Actual Duration: ").append(getActualDurationMinutes()).append(" minutes\n");
        }
        
        info.append("Created: ").append(getCreatedAt().format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT))).append("\n");
        info.append("Last Updated: ").append(getUpdatedAt().format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT))).append("\n");
        info.append("=".repeat(50)).append("\n");
        
        return info.toString();
    }
}