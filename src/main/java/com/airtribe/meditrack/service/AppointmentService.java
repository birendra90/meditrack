package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.AppointmentStatus;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.exception.AppointmentNotFoundException;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;
import com.airtribe.meditrack.util.DateUtil;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.function.Predicate;

/**
 * Service class for managing Appointment entities and operations.
 * This demonstrates complex business logic, scheduling, and coordination between entities.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class AppointmentService {
    
    private final DataStore<Appointment> appointmentStore;
    private final IdGenerator idGenerator;
    private final DoctorService doctorService;
    private final PatientService patientService;
    
    // Predefined comparators for different sorting needs
    public static final Comparator<Appointment> BY_DATE_TIME = 
            Comparator.comparing(Appointment::getAppointmentDateTime);
    
    public static final Comparator<Appointment> BY_STATUS = 
            Comparator.comparing(appointment -> appointment.getStatus().getPriority());
    
    public static final Comparator<Appointment> BY_PATIENT_NAME = 
            Comparator.comparing(appointment -> {
                // This would ideally resolve patient name from service
                return appointment.getPatientId();
            });
    
    public static final Comparator<Appointment> BY_DOCTOR_NAME = 
            Comparator.comparing(appointment -> {
                // This would ideally resolve doctor name from service
                return appointment.getDoctorId();
            });
    
    public static final Comparator<Appointment> BY_PRIORITY = 
            Comparator.comparing(Appointment::getPriority);
    
    /**
     * Constructor that initializes the service with data stores and related services.
     * 
     * @param doctorService the doctor service for coordination
     * @param patientService the patient service for coordination
     */
    public AppointmentService(DoctorService doctorService, PatientService patientService) {
        this.appointmentStore = new DataStore<>("Appointment");
        this.idGenerator = IdGenerator.getInstance();
        this.doctorService = doctorService;
        this.patientService = patientService;
        
        // Set default comparator for the store
        this.appointmentStore.setDefaultComparator(BY_DATE_TIME);
    }
    
    /**
     * Constructor with external data store (for dependency injection).
     */
    public AppointmentService(DataStore<Appointment> appointmentStore, 
                            DoctorService doctorService, PatientService patientService) {
        this.appointmentStore = appointmentStore;
        this.idGenerator = IdGenerator.getInstance();
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentStore.setDefaultComparator(BY_DATE_TIME);
    }
    
    // CRUD Operations
    
    /**
     * Creates a new appointment.
     * 
     * @param patientId the patient ID
     * @param doctorId the doctor ID
     * @param appointmentDateTime the appointment date and time
     * @param durationMinutes the duration in minutes
     * @param reasonForVisit the reason for the visit
     * @param appointmentType the type of appointment
     * @param isEmergency whether this is an emergency appointment
     * @return the created appointment
     * @throws InvalidDataException if validation fails or scheduling conflicts exist
     */
    public Appointment createAppointment(String patientId, String doctorId, 
                                       LocalDateTime appointmentDateTime, int durationMinutes,
                                       String reasonForVisit, String appointmentType, 
                                       boolean isEmergency) throws InvalidDataException {
        
        // Validate basic data
        validateAppointmentData(patientId, doctorId, appointmentDateTime, durationMinutes, reasonForVisit);
        
        // Check if patient exists
        Patient patient = patientService.getPatientById(patientId);
        if (patient == null || !patient.isActive()) {
            throw new InvalidDataException("Patient with ID '" + patientId + "' not found or inactive");
        }
        
        // Check if doctor exists and is available
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null || !doctor.isActive()) {
            throw new InvalidDataException("Doctor with ID '" + doctorId + "' not found or inactive");
        }
        
        if (!doctor.isAvailable()) {
            throw new InvalidDataException("Doctor is not available for appointments");
        }
        
        // Check for scheduling conflicts
        validateSchedulingConflicts(doctorId, appointmentDateTime, durationMinutes, null);
        
        // Generate new ID
        String appointmentId = idGenerator.generateAppointmentId();
        
        // Create appointment entity
        Appointment appointment = new Appointment(appointmentId, patientId, doctorId, 
                                                appointmentDateTime, durationMinutes,
                                                reasonForVisit, appointmentType,
                                                doctor.getEffectiveConsultationFee(isEmergency), 
                                                isEmergency);
        
        // Set references for easy access
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        
        // Store the appointment
        appointmentStore.store(appointmentId, appointment);
        
        // Record visit for patient
        try {
            patientService.recordVisit(patientId);
        } catch (InvalidDataException e) {
            // Log warning but don't fail appointment creation
            System.err.println("Warning: Could not record visit for patient: " + e.getMessage());
        }
        
        return appointment;
    }
    
    /**
     * Retrieves an appointment by ID.
     * 
     * @param appointmentId the appointment ID
     * @return the appointment
     * @throws AppointmentNotFoundException if appointment not found
     */
    public Appointment getAppointmentById(String appointmentId) throws AppointmentNotFoundException {
        Appointment appointment = appointmentStore.get(appointmentId);
        if (appointment == null) {
            throw AppointmentNotFoundException.forAppointmentId(appointmentId);
        }
        return appointment;
    }
    
    /**
     * Updates an existing appointment.
     * 
     * @param appointmentId the appointment ID
     * @param updatedAppointment the updated appointment data
     * @return the updated appointment
     * @throws InvalidDataException if validation fails
     * @throws AppointmentNotFoundException if appointment not found
     */
    public Appointment updateAppointment(String appointmentId, Appointment updatedAppointment) 
            throws InvalidDataException, AppointmentNotFoundException {
        
        Appointment existingAppointment = getAppointmentById(appointmentId);
        
        // Check if appointment allows modifications
        if (!existingAppointment.getStatus().allowsModification()) {
            throw new InvalidDataException("Cannot modify appointment with status: " + 
                                         existingAppointment.getStatus());
        }
        
        // Validate the updated appointment
        updatedAppointment.validateAndThrow();
        
        // Check for scheduling conflicts (excluding current appointment)
        if (!existingAppointment.getAppointmentDateTime().equals(updatedAppointment.getAppointmentDateTime()) ||
            existingAppointment.getDurationMinutes() != updatedAppointment.getDurationMinutes()) {
            
            validateSchedulingConflicts(updatedAppointment.getDoctorId(), 
                                       updatedAppointment.getAppointmentDateTime(),
                                       updatedAppointment.getDurationMinutes(), 
                                       appointmentId);
        }
        
        // Ensure ID remains the same
        updatedAppointment.setId(appointmentId);
        
        // Update in store
        appointmentStore.update(appointmentId, updatedAppointment);
        
        return updatedAppointment;
    }
    
    /**
     * Deletes an appointment by ID.
     * 
     * @param appointmentId the appointment ID
     * @return true if deleted, false if not found
     */
    public boolean deleteAppointment(String appointmentId) {
        Appointment removed = appointmentStore.remove(appointmentId);
        return removed != null;
    }
    
    // Appointment Lifecycle Management
    
    /**
     * Confirms a pending appointment.
     * 
     * @param appointmentId the appointment ID
     * @throws AppointmentNotFoundException if appointment not found
     * @throws InvalidDataException if confirmation is not allowed
     */
    public void confirmAppointment(String appointmentId) 
            throws AppointmentNotFoundException, InvalidDataException {
        
        Appointment appointment = getAppointmentById(appointmentId);
        
        try {
            appointment.confirm();
        } catch (IllegalStateException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
    
    /**
     * Starts an appointment (marks as in progress).
     * 
     * @param appointmentId the appointment ID
     * @throws AppointmentNotFoundException if appointment not found
     * @throws InvalidDataException if starting is not allowed
     */
    public void startAppointment(String appointmentId) 
            throws AppointmentNotFoundException, InvalidDataException {
        
        Appointment appointment = getAppointmentById(appointmentId);
        
        try {
            appointment.start();
        } catch (IllegalStateException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
    
    /**
     * Completes an appointment with diagnosis and prescription.
     * 
     * @param appointmentId the appointment ID
     * @param diagnosis the diagnosis
     * @param prescription the prescription (optional)
     * @param notes additional notes (optional)
     * @throws AppointmentNotFoundException if appointment not found
     * @throws InvalidDataException if completion is not allowed
     */
    public void completeAppointment(String appointmentId, String diagnosis, 
                                  String prescription, String notes) 
            throws AppointmentNotFoundException, InvalidDataException {
        
        Appointment appointment = getAppointmentById(appointmentId);
        
        try {
            appointment.complete(diagnosis, prescription, notes);
        } catch (IllegalStateException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
    
    /**
     * Cancels an appointment with a reason.
     * 
     * @param appointmentId the appointment ID
     * @param reason the cancellation reason
     * @throws AppointmentNotFoundException if appointment not found
     * @throws InvalidDataException if cancellation is not allowed
     */
    public void cancelAppointment(String appointmentId, String reason) 
            throws AppointmentNotFoundException, InvalidDataException {
        
        Appointment appointment = getAppointmentById(appointmentId);
        
        try {
            appointment.cancel(reason);
        } catch (IllegalStateException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
    
    /**
     * Reschedules an appointment to a new date/time.
     * 
     * @param appointmentId the appointment ID
     * @param newDateTime the new appointment date/time
     * @param reason the reason for rescheduling
     * @throws AppointmentNotFoundException if appointment not found
     * @throws InvalidDataException if rescheduling is not allowed or conflicts exist
     */
    public void rescheduleAppointment(String appointmentId, LocalDateTime newDateTime, String reason) 
            throws AppointmentNotFoundException, InvalidDataException {
        
        Appointment appointment = getAppointmentById(appointmentId);
        
        // Check for scheduling conflicts
        validateSchedulingConflicts(appointment.getDoctorId(), newDateTime, 
                                   appointment.getDurationMinutes(), appointmentId);
        
        try {
            appointment.reschedule(newDateTime, reason);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
    
    /**
     * Marks an appointment as no-show.
     * 
     * @param appointmentId the appointment ID
     * @throws AppointmentNotFoundException if appointment not found
     * @throws InvalidDataException if marking as no-show is not allowed
     */
    public void markNoShow(String appointmentId) 
            throws AppointmentNotFoundException, InvalidDataException {
        
        Appointment appointment = getAppointmentById(appointmentId);
        
        try {
            appointment.markNoShow();
        } catch (IllegalStateException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
    
    // Query Operations
    
    /**
     * Gets all appointments.
     * 
     * @return list of all active appointments
     */
    public List<Appointment> getAllAppointments() {
        return appointmentStore.getAll().stream()
                .filter(Appointment::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all appointments sorted by date/time.
     * 
     * @return sorted list of appointments
     */
    public List<Appointment> getAllAppointmentsSorted() {
        return appointmentStore.getAllSorted(BY_DATE_TIME).stream()
                .filter(Appointment::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets appointments for a specific patient.
     * 
     * @param patientId the patient ID
     * @return list of patient's appointments
     * @throws AppointmentNotFoundException if no appointments found for patient
     */
    public List<Appointment> getAppointmentsByPatient(String patientId) throws AppointmentNotFoundException {
        List<Appointment> appointments = appointmentStore.findWhere(appointment -> 
                appointment.isActive() && 
                patientId.equals(appointment.getPatientId()));
        
        if (appointments.isEmpty()) {
            throw AppointmentNotFoundException.forPatient(patientId);
        }
        
        return appointments.stream()
                .sorted(BY_DATE_TIME)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets appointments for a specific doctor.
     * 
     * @param doctorId the doctor ID
     * @return list of doctor's appointments
     * @throws AppointmentNotFoundException if no appointments found for doctor
     */
    public List<Appointment> getAppointmentsByDoctor(String doctorId) throws AppointmentNotFoundException {
        List<Appointment> appointments = appointmentStore.findWhere(appointment -> 
                appointment.isActive() && 
                doctorId.equals(appointment.getDoctorId()));
        
        if (appointments.isEmpty()) {
            throw AppointmentNotFoundException.forDoctor(doctorId);
        }
        
        return appointments.stream()
                .sorted(BY_DATE_TIME)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets appointments for a specific date.
     * 
     * @param date the date to search for
     * @return list of appointments on that date
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentStore.findWhere(appointment -> 
                appointment.isActive() &&
                appointment.getAppointmentDateTime() != null &&
                appointment.getAppointmentDateTime().toLocalDate().equals(date));
    }
    
    /**
     * Gets appointments within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of appointments within the range
     */
    public List<Appointment> getAppointmentsBetween(LocalDate startDate, LocalDate endDate) {
        return appointmentStore.findWhere(appointment -> 
                appointment.isActive() &&
                appointment.getAppointmentDateTime() != null &&
                !appointment.getAppointmentDateTime().toLocalDate().isBefore(startDate) &&
                !appointment.getAppointmentDateTime().toLocalDate().isAfter(endDate));
    }
    
    /**
     * Gets appointments by status.
     * 
     * @param status the appointment status
     * @return list of appointments with the specified status
     */
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentStore.findWhere(appointment -> 
                appointment.isActive() && 
                appointment.getStatus() == status);
    }
    
    /**
     * Gets today's appointments.
     * 
     * @return list of today's appointments
     */
    public List<Appointment> getTodaysAppointments() {
        return getAppointmentsByDate(LocalDate.now());
    }
    
    /**
     * Gets upcoming appointments (future appointments).
     * 
     * @return list of upcoming appointments
     */
    public List<Appointment> getUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        return appointmentStore.findWhere(appointment -> 
                appointment.isActive() &&
                appointment.getAppointmentDateTime() != null &&
                appointment.getAppointmentDateTime().isAfter(now) &&
                !appointment.getStatus().isFinal()).stream()
                .sorted(BY_DATE_TIME)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets overdue appointments.
     * 
     * @return list of overdue appointments
     */
    public List<Appointment> getOverdueAppointments() {
        return appointmentStore.findWhere(appointment -> 
                appointment.isActive() && appointment.isOverdue());
    }
    
    /**
     * Gets emergency appointments.
     * 
     * @return list of emergency appointments
     */
    public List<Appointment> getEmergencyAppointments() {
        return appointmentStore.findWhere(appointment -> 
                appointment.isActive() && appointment.isEmergency());
    }
    
    // Search and Advanced Queries
    
    /**
     * Searches appointments using various criteria.
     * 
     * @param searchTerm the search term
     * @return list of matching appointments sorted by relevance
     */
    public List<Appointment> searchAppointments(String searchTerm) {
        return appointmentStore.searchAndSort(searchTerm).stream()
                .filter(Appointment::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Advanced search with multiple criteria.
     * 
     * @param patientId patient ID filter (optional)
     * @param doctorId doctor ID filter (optional)
     * @param status status filter (optional)
     * @param startDate start date filter (optional)
     * @param endDate end date filter (optional)
     * @param appointmentType type filter (optional)
     * @param emergencyOnly emergency appointments only
     * @return list of matching appointments
     */
    public List<Appointment> advancedSearch(String patientId, String doctorId, 
                                          AppointmentStatus status, LocalDate startDate, 
                                          LocalDate endDate, String appointmentType, 
                                          boolean emergencyOnly) {
        
        Predicate<Appointment> criteria = appointment -> appointment.isActive();
        
        // Add patient filter
        if (patientId != null && !patientId.trim().isEmpty()) {
            criteria = criteria.and(appointment -> 
                    patientId.equals(appointment.getPatientId()));
        }
        
        // Add doctor filter
        if (doctorId != null && !doctorId.trim().isEmpty()) {
            criteria = criteria.and(appointment -> 
                    doctorId.equals(appointment.getDoctorId()));
        }
        
        // Add status filter
        if (status != null) {
            criteria = criteria.and(appointment -> appointment.getStatus() == status);
        }
        
        // Add date filters
        if (startDate != null) {
            criteria = criteria.and(appointment -> 
                    appointment.getAppointmentDateTime() != null &&
                    !appointment.getAppointmentDateTime().toLocalDate().isBefore(startDate));
        }
        if (endDate != null) {
            criteria = criteria.and(appointment -> 
                    appointment.getAppointmentDateTime() != null &&
                    !appointment.getAppointmentDateTime().toLocalDate().isAfter(endDate));
        }
        
        // Add type filter
        if (appointmentType != null && !appointmentType.trim().isEmpty()) {
            criteria = criteria.and(appointment -> 
                    appointmentType.equalsIgnoreCase(appointment.getAppointmentType()));
        }
        
        // Add emergency filter
        if (emergencyOnly) {
            criteria = criteria.and(Appointment::isEmergency);
        }
        
        return appointmentStore.findWhere(criteria);
    }
    
    // Business Logic Methods
    
    /**
     * Gets available appointment slots for a doctor on a specific date.
     * 
     * @param doctorId the doctor ID
     * @param date the date to check
     * @param slotDurationMinutes the duration of each slot
     * @return list of available appointment times
     */
    public List<LocalDateTime> getAvailableSlots(String doctorId, LocalDate date, int slotDurationMinutes) {
        // Generate all possible slots for the day
        List<LocalDateTime> allSlots = DateUtil.generateAppointmentSlots(date, slotDurationMinutes);
        
        // Get existing appointments for the doctor on this date
        List<Appointment> existingAppointments = appointmentStore.findWhere(appointment ->
                appointment.isActive() &&
                doctorId.equals(appointment.getDoctorId()) &&
                appointment.getAppointmentDateTime() != null &&
                appointment.getAppointmentDateTime().toLocalDate().equals(date) &&
                !appointment.getStatus().isFinal());
        
        // Filter out occupied slots
        return allSlots.stream()
                .filter(slot -> {
                    LocalDateTime slotEnd = slot.plusMinutes(slotDurationMinutes);
                    
                    return existingAppointments.stream()
                            .noneMatch(appointment -> {
                                LocalDateTime appointmentStart = appointment.getAppointmentDateTime();
                                LocalDateTime appointmentEnd = appointmentStart.plusMinutes(appointment.getDurationMinutes());
                                
                                return DateUtil.doAppointmentsOverlap(slot, slotEnd, appointmentStart, appointmentEnd);
                            });
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Finds next available appointment slot for a doctor.
     * 
     * @param doctorId the doctor ID
     * @param preferredDate the preferred date (will search from this date onwards)
     * @param durationMinutes the duration needed
     * @return the next available slot, or null if none found within reasonable timeframe
     */
    public LocalDateTime findNextAvailableSlot(String doctorId, LocalDate preferredDate, int durationMinutes) {
        LocalDate searchDate = preferredDate;
        
        // Search up to 30 days ahead
        for (int i = 0; i < 30; i++) {
            List<LocalDateTime> availableSlots = getAvailableSlots(doctorId, searchDate, durationMinutes);
            
            if (!availableSlots.isEmpty()) {
                return availableSlots.get(0); // Return the first available slot
            }
            
            searchDate = DateUtil.getNextWeekday(searchDate);
        }
        
        return null; // No available slot found
    }
    
    /**
     * Sends appointment reminders (simulated).
     * 
     * @return list of appointments that received reminders
     */
    public List<Appointment> sendAppointmentReminders() {
        List<Appointment> remindersent = appointmentStore.findWhere(appointment ->
                appointment.isActive() && appointment.needsReminder());
        
        for (Appointment appointment : remindersent) {
            appointment.markReminderSent();
            // In a real system, this would send actual notifications
            System.out.println("Reminder sent for appointment: " + appointment.getId());
        }
        
        return remindersent;
    }
    
    /**
     * Gets appointment statistics.
     * 
     * @return formatted statistics string
     */
    public String getAppointmentStatistics() {
        List<Appointment> allAppointments = getAllAppointments();
        
        if (allAppointments.isEmpty()) {
            return "No appointments found in the system.";
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("Appointment Statistics:\n");
        stats.append("=".repeat(40)).append("\n");
        stats.append("Total Appointments: ").append(allAppointments.size()).append("\n");
        
        // Count by status
        Map<AppointmentStatus, Long> statusCounts = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        
        stats.append("\nBy Status:\n");
        statusCounts.entrySet().stream()
                .sorted(Map.Entry.<AppointmentStatus, Long>comparingByValue().reversed())
                .forEach(entry -> stats.append("  ")
                        .append(entry.getKey().getDisplayName())
                        .append(": ").append(entry.getValue()).append("\n"));
        
        // Count by type
        Map<String, Long> typeCounts = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentType, Collectors.counting()));
        
        stats.append("\nBy Type:\n");
        typeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> stats.append("  ")
                        .append(entry.getKey())
                        .append(": ").append(entry.getValue()).append("\n"));
        
        // Emergency appointments
        long emergencyAppointments = allAppointments.stream()
                .filter(Appointment::isEmergency)
                .count();
        
        stats.append("\nEmergency Appointments: ").append(emergencyAppointments).append("\n");
        
        // Today's statistics
        List<Appointment> todaysAppointments = getTodaysAppointments();
        stats.append("Today's Appointments: ").append(todaysAppointments.size()).append("\n");
        
        // Overdue appointments
        List<Appointment> overdueAppointments = getOverdueAppointments();
        stats.append("Overdue Appointments: ").append(overdueAppointments.size()).append("\n");
        
        return stats.toString();
    }
    
    // Validation Methods
    
    /**
     * Validates appointment data.
     */
    private void validateAppointmentData(String patientId, String doctorId, 
                                       LocalDateTime appointmentDateTime, int durationMinutes, 
                                       String reasonForVisit) throws InvalidDataException {
        
        Validator.validateNotNull(patientId, "patientId");
        Validator.validateNotNull(doctorId, "doctorId");
        Validator.validateAppointmentDateTime(appointmentDateTime);
        Validator.validateAppointmentDuration(durationMinutes);
        Validator.validateRequired(reasonForVisit, "reasonForVisit");
    }
    
    /**
     * Validates scheduling conflicts for appointments.
     */
    private void validateSchedulingConflicts(String doctorId, LocalDateTime appointmentDateTime, 
                                           int durationMinutes, String excludeAppointmentId) 
                                           throws InvalidDataException {
        
        LocalDateTime endTime = appointmentDateTime.plusMinutes(durationMinutes);
        
        List<Appointment> conflictingAppointments = appointmentStore.findWhere(appointment ->
                appointment.isActive() &&
                doctorId.equals(appointment.getDoctorId()) &&
                !appointment.getStatus().isFinal() &&
                (excludeAppointmentId == null || !excludeAppointmentId.equals(appointment.getId())));
        
        for (Appointment existing : conflictingAppointments) {
            LocalDateTime existingStart = existing.getAppointmentDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
            
            if (DateUtil.doAppointmentsOverlap(appointmentDateTime, endTime, existingStart, existingEnd)) {
                throw new InvalidDataException("Appointment conflicts with existing appointment " + 
                                             existing.getId() + " at " + 
                                             DateUtil.formatDateTime(existingStart));
            }
        }
    }
    
    // Utility Methods
    
    /**
     * Gets the count of active appointments.
     * 
     * @return count of active appointments
     */
    public int getActiveAppointmentCount() {
        return (int) appointmentStore.count(Appointment::isActive);
    }
    
    /**
     * Gets the data store for external operations.
     * 
     * @return the data store
     */
    public DataStore<Appointment> getDataStore() {
        return appointmentStore;
    }
}