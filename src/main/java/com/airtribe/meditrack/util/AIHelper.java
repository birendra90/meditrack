package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Specialization;
import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.AppointmentStatus;
import com.airtribe.meditrack.service.DoctorService;
import com.airtribe.meditrack.service.AppointmentService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AIHelper provides intelligent recommendations for the MediTrack system.
 * This class implements rule-based AI features for doctor recommendations
 * and appointment slot suggestions.
 * 
 * Features:
 * - Rule-based doctor recommendation by symptoms
 * - Auto-suggest appointment slots based on availability
 * - Smart scheduling algorithms
 * 
 * @author MediTrack Team
 * @version 1.0
 */
public class AIHelper {
    
    // Symptom to specialization mapping
    private static final Map<String, Specialization> SYMPTOM_SPECIALIZATION_MAP;
    
    // Working hours configuration
    private static final LocalTime CLINIC_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLINIC_END_TIME = LocalTime.of(17, 0);
    private static final int APPOINTMENT_DURATION_MINUTES = 30;
    
    static {
        // Initialize symptom to specialization mapping
        SYMPTOM_SPECIALIZATION_MAP = new HashMap<>();
        
        // Cardiology symptoms
        SYMPTOM_SPECIALIZATION_MAP.put("chest pain", Specialization.CARDIOLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("heart palpitations", Specialization.CARDIOLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("shortness of breath", Specialization.CARDIOLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("high blood pressure", Specialization.CARDIOLOGY);
        
        // Dermatology symptoms
        SYMPTOM_SPECIALIZATION_MAP.put("skin rash", Specialization.DERMATOLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("acne", Specialization.DERMATOLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("skin infection", Specialization.DERMATOLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("mole changes", Specialization.DERMATOLOGY);
        
        // Neurology symptoms
        SYMPTOM_SPECIALIZATION_MAP.put("headache", Specialization.NEUROLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("migraine", Specialization.NEUROLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("seizure", Specialization.NEUROLOGY);
        SYMPTOM_SPECIALIZATION_MAP.put("memory loss", Specialization.NEUROLOGY);
        
        // Orthopedics symptoms
        SYMPTOM_SPECIALIZATION_MAP.put("bone pain", Specialization.ORTHOPEDICS);
        SYMPTOM_SPECIALIZATION_MAP.put("joint pain", Specialization.ORTHOPEDICS);
        SYMPTOM_SPECIALIZATION_MAP.put("back pain", Specialization.ORTHOPEDICS);
        SYMPTOM_SPECIALIZATION_MAP.put("fracture", Specialization.ORTHOPEDICS);
        
        // Pediatrics symptoms
        SYMPTOM_SPECIALIZATION_MAP.put("child fever", Specialization.PEDIATRICS);
        SYMPTOM_SPECIALIZATION_MAP.put("infant care", Specialization.PEDIATRICS);
        SYMPTOM_SPECIALIZATION_MAP.put("vaccination", Specialization.PEDIATRICS);
        
        // General Medicine (fallback)
        SYMPTOM_SPECIALIZATION_MAP.put("fever", Specialization.GENERAL_MEDICINE);
        SYMPTOM_SPECIALIZATION_MAP.put("cold", Specialization.GENERAL_MEDICINE);
        SYMPTOM_SPECIALIZATION_MAP.put("cough", Specialization.GENERAL_MEDICINE);
        SYMPTOM_SPECIALIZATION_MAP.put("flu", Specialization.GENERAL_MEDICINE);
    }
    
    /**
     * Recommends doctors based on patient symptoms using rule-based AI.
     * 
     * @param symptoms List of symptoms reported by the patient
     * @param doctorService Service to access doctor data
     * @return List of recommended doctors sorted by relevance
     */
    public static List<Doctor> recommendDoctorsBySymptoms(List<String> symptoms, DoctorService doctorService) {
        if (symptoms == null || symptoms.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Count specialization matches
        Map<Specialization, Integer> specializationScores = new HashMap<>();
        
        for (String symptom : symptoms) {
            String normalizedSymptom = symptom.toLowerCase().trim();
            
            // Find matching specialization
            Specialization matchedSpecialization = findBestSpecializationMatch(normalizedSymptom);
            
            if (matchedSpecialization != null) {
                specializationScores.merge(matchedSpecialization, 1, Integer::sum);
            }
        }
        
        // If no specific matches found, default to General Medicine
        if (specializationScores.isEmpty()) {
            specializationScores.put(Specialization.GENERAL_MEDICINE, 1);
        }
        
        // Get doctors and score them
        List<Doctor> allDoctors = doctorService.getAllDoctors();
        List<DoctorRecommendation> recommendations = new ArrayList<>();
        
        for (Doctor doctor : allDoctors) {
            int score = specializationScores.getOrDefault(doctor.getSpecialization(), 0);
            if (score > 0) {
                recommendations.add(new DoctorRecommendation(doctor, score));
            }
        }
        
        // Sort by score (descending) and then by doctor experience/rating
        return recommendations.stream()
                .sorted((r1, r2) -> {
                    int scoreCompare = Integer.compare(r2.score, r1.score);
                    if (scoreCompare != 0) return scoreCompare;
                    
                    // Secondary sort by doctor experience (assuming years of experience)
                    return Integer.compare(r2.doctor.getYearsOfExperience(), r1.doctor.getYearsOfExperience());
                })
                .map(r -> r.doctor)
                .collect(Collectors.toList());
    }
    
    /**
     * Suggests available appointment slots for a doctor on a given date.
     * 
     * @param doctorId Doctor for whom to suggest slots
     * @param date Date for appointment
     * @param appointmentService Service to check existing appointments
     * @return List of available time slots
     */
    public static List<LocalDateTime> suggestAppointmentSlots(String doctorId,
                                                            LocalDateTime date,
                                                            AppointmentService appointmentService) {
        List<LocalDateTime> availableSlots = new ArrayList<>();
        
        // Get existing appointments for the doctor on the given date
        List<Appointment> existingAppointments = appointmentService.getAppointmentsByDate(date.toLocalDate()).stream()
                .filter(apt -> doctorId.equals(apt.getDoctorId()))
                .collect(Collectors.toList());
        
        // Extract booked time slots
        Set<LocalDateTime> bookedSlots = existingAppointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED ||
                              apt.getStatus() == AppointmentStatus.PENDING)
                .map(Appointment::getAppointmentDateTime)
                .collect(Collectors.toSet());
        
        // Generate all possible slots for the day
        LocalDateTime currentSlot = date.toLocalDate().atTime(CLINIC_START_TIME);
        LocalDateTime endOfDay = date.toLocalDate().atTime(CLINIC_END_TIME);
        
        while (currentSlot.isBefore(endOfDay)) {
            if (!bookedSlots.contains(currentSlot)) {
                availableSlots.add(currentSlot);
            }
            currentSlot = currentSlot.plusMinutes(APPOINTMENT_DURATION_MINUTES);
        }
        
        return availableSlots;
    }
    
    /**
     * Finds the best matching specialization for a symptom.
     * Uses fuzzy matching for better results.
     */
    private static Specialization findBestSpecializationMatch(String symptom) {
        // Direct match
        if (SYMPTOM_SPECIALIZATION_MAP.containsKey(symptom)) {
            return SYMPTOM_SPECIALIZATION_MAP.get(symptom);
        }
        
        // Fuzzy matching - check if symptom contains any keyword
        for (Map.Entry<String, Specialization> entry : SYMPTOM_SPECIALIZATION_MAP.entrySet()) {
            String keyword = entry.getKey();
            if (symptom.contains(keyword) || keyword.contains(symptom)) {
                return entry.getValue();
            }
        }
        
        // No match found
        return Specialization.GENERAL_MEDICINE; // Default fallback
    }
    
    /**
     * Calculates the optimal appointment duration based on appointment type and doctor specialization.
     * 
     * @param specialization Doctor's specialization
     * @param appointmentType Type of appointment (consultation, follow-up, etc.)
     * @return Recommended duration in minutes
     */
    public static int calculateOptimalDuration(Specialization specialization, String appointmentType) {
        // Base duration
        int baseDuration = APPOINTMENT_DURATION_MINUTES;
        
        // Adjust based on specialization
        switch (specialization) {
            case GENERAL_MEDICINE:
                baseDuration = 20;
                break;
            case CARDIOLOGY:
            case NEUROLOGY:
                baseDuration = 45;
                break;
            case DERMATOLOGY:
                baseDuration = 25;
                break;
            case ORTHOPEDICS:
                baseDuration = 35;
                break;
            case PEDIATRICS:
                baseDuration = 30;
                break;
        }
        
        // Adjust based on appointment type
        if (appointmentType != null) {
            switch (appointmentType.toLowerCase()) {
                case "consultation":
                case "new patient":
                    baseDuration += 15; // New patients need more time
                    break;
                case "follow-up":
                    baseDuration -= 10; // Follow-ups are typically shorter
                    break;
                case "emergency":
                    baseDuration += 20; // Emergency cases need more time
                    break;
            }
        }
        
        return Math.max(15, baseDuration); // Minimum 15 minutes
    }
    
    /**
     * Analyzes appointment patterns and suggests optimal scheduling.
     * 
     * @param appointments Historical appointment data
     * @return Scheduling insights and recommendations
     */
    public static SchedulingInsights analyzeAppointmentPatterns(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            return new SchedulingInsights();
        }
        
        // Analyze peak hours
        Map<Integer, Long> hourlyDistribution = appointments.stream()
                .filter(apt -> apt.getAppointmentDateTime() != null)
                .collect(Collectors.groupingBy(
                        apt -> apt.getAppointmentDateTime().getHour(),
                        Collectors.counting()
                ));
        
        // Find peak hour
        int peakHour = hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(10); // Default to 10 AM
        
        // Analyze cancellation patterns
        long cancellationRate = appointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.CANCELLED)
                .count() * 100 / appointments.size();
        
        return new SchedulingInsights(peakHour, cancellationRate, hourlyDistribution);
    }
    
    /**
     * Helper class for doctor recommendations with scoring
     */
    private static class DoctorRecommendation {
        final Doctor doctor;
        final int score;
        
        DoctorRecommendation(Doctor doctor, int score) {
            this.doctor = doctor;
            this.score = score;
        }
    }
    
    /**
     * Class to hold scheduling insights and analytics
     */
    public static class SchedulingInsights {
        private final int peakHour;
        private final long cancellationRate;
        private final Map<Integer, Long> hourlyDistribution;
        
        public SchedulingInsights() {
            this.peakHour = 10;
            this.cancellationRate = 0;
            this.hourlyDistribution = new HashMap<>();
        }
        
        public SchedulingInsights(int peakHour, long cancellationRate, Map<Integer, Long> hourlyDistribution) {
            this.peakHour = peakHour;
            this.cancellationRate = cancellationRate;
            this.hourlyDistribution = new HashMap<>(hourlyDistribution);
        }
        
        public int getPeakHour() { return peakHour; }
        public long getCancellationRate() { return cancellationRate; }
        public Map<Integer, Long> getHourlyDistribution() { return new HashMap<>(hourlyDistribution); }
        
        @Override
        public String toString() {
            return String.format("SchedulingInsights{peakHour=%d:00, cancellationRate=%d%%, busiest periods=%s}",
                    peakHour, cancellationRate, 
                    hourlyDistribution.entrySet().stream()
                            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                            .limit(3)
                            .collect(Collectors.toList()));
        }
    }
}