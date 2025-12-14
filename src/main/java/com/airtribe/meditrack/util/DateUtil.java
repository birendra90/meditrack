package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Period;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for date and time operations.
 * This demonstrates utility class design and date/time manipulation.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public final class DateUtil {
    
    // Predefined formatters for better performance
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIME_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT);
    
    // Alternative formatters
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
    
    // Private constructor to prevent instantiation
    private DateUtil() {
        throw new UnsupportedOperationException("DateUtil is a utility class and cannot be instantiated");
    }
    
    // Date/Time Formatting Methods
    
    /**
     * Formats a LocalDate using the application's standard date format.
     * 
     * @param date the date to format
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    /**
     * Formats a LocalTime using the application's standard time format.
     * 
     * @param time the time to format
     * @return formatted time string
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }
    
    /**
     * Formats a LocalDateTime using the application's standard datetime format.
     * 
     * @param dateTime the datetime to format
     * @return formatted datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
    
    /**
     * Formats a LocalDate for display purposes.
     * 
     * @param date the date to format
     * @return formatted display date string (e.g., "Monday, January 15, 2025")
     */
    public static String formatDisplayDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : null;
    }
    
    /**
     * Formats a LocalDateTime for display purposes.
     * 
     * @param dateTime the datetime to format
     * @return formatted display datetime string (e.g., "Monday, January 15, 2025 at 02:30 PM")
     */
    public static String formatDisplayDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMATTER) : null;
    }
    
    // Date/Time Parsing Methods
    
    /**
     * Parses a date string using the application's standard format.
     * 
     * @param dateString the date string to parse
     * @return parsed LocalDate
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDate parseDate(String dateString) throws DateTimeParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
    }
    
    /**
     * Parses a time string using the application's standard format.
     * 
     * @param timeString the time string to parse
     * @return parsed LocalTime
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalTime parseTime(String timeString) throws DateTimeParseException {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeString.trim(), TIME_FORMATTER);
    }
    
    /**
     * Parses a datetime string using the application's standard format.
     * 
     * @param dateTimeString the datetime string to parse
     * @return parsed LocalDateTime
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeString) throws DateTimeParseException {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString.trim(), DATETIME_FORMATTER);
    }
    
    /**
     * Safely parses a date string, returning null if parsing fails.
     * 
     * @param dateString the date string to parse
     * @return parsed LocalDate or null if parsing fails
     */
    public static LocalDate safeParse(String dateString) {
        try {
            return parseDate(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Safely parses a datetime string, returning null if parsing fails.
     * 
     * @param dateTimeString the datetime string to parse
     * @return parsed LocalDateTime or null if parsing fails
     */
    public static LocalDateTime safeParseDateTime(String dateTimeString) {
        try {
            return parseDateTime(dateTimeString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    // Date/Time Calculation Methods
    
    /**
     * Calculates age in years based on date of birth.
     * 
     * @param dateOfBirth the date of birth
     * @return age in years
     */
    public static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    
    /**
     * Calculates age at a specific date.
     * 
     * @param dateOfBirth the date of birth
     * @param atDate the date to calculate age at
     * @return age in years at the specified date
     */
    public static int calculateAgeAt(LocalDate dateOfBirth, LocalDate atDate) {
        if (dateOfBirth == null || atDate == null) {
            return 0;
        }
        return Period.between(dateOfBirth, atDate).getYears();
    }
    
    /**
     * Calculates the duration between two datetime instances in minutes.
     * 
     * @param start the start datetime
     * @param end the end datetime
     * @return duration in minutes
     */
    public static long getDurationInMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toMinutes();
    }
    
    /**
     * Calculates the duration between two datetime instances in hours.
     * 
     * @param start the start datetime
     * @param end the end datetime
     * @return duration in hours
     */
    public static long getDurationInHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toHours();
    }
    
    /**
     * Calculates the number of days between two dates.
     * 
     * @param start the start date
     * @param end the end date
     * @return number of days between the dates
     */
    public static long getDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }
    
    // Date/Time Validation Methods
    
    /**
     * Checks if a date is today.
     * 
     * @param date the date to check
     * @return true if the date is today
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }
    
    /**
     * Checks if a datetime is today.
     * 
     * @param dateTime the datetime to check
     * @return true if the datetime is today
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }
    
    /**
     * Checks if a date is in the past.
     * 
     * @param date the date to check
     * @return true if the date is before today
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if a datetime is in the past.
     * 
     * @param dateTime the datetime to check
     * @return true if the datetime is before now
     */
    public static boolean isPastDateTime(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Checks if a date is in the future.
     * 
     * @param date the date to check
     * @return true if the date is after today
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
    
    /**
     * Checks if a datetime is in the future.
     * 
     * @param dateTime the datetime to check
     * @return true if the datetime is after now
     */
    public static boolean isFutureDateTime(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Checks if a datetime falls within business hours.
     * 
     * @param dateTime the datetime to check
     * @return true if within business hours
     */
    public static boolean isWithinBusinessHours(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        int hour = dateTime.getHour();
        return hour >= Constants.CLINIC_START_HOUR && hour < Constants.CLINIC_END_HOUR;
    }
    
    /**
     * Checks if a date is a weekday.
     * 
     * @param date the date to check
     * @return true if the date is a weekday (Monday to Friday)
     */
    public static boolean isWeekday(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    
    /**
     * Checks if a date is a weekend.
     * 
     * @param date the date to check
     * @return true if the date is a weekend (Saturday or Sunday)
     */
    public static boolean isWeekend(LocalDate date) {
        return !isWeekday(date);
    }
    
    // Date/Time Generation Methods
    
    /**
     * Gets the start of the day for a given date.
     * 
     * @param date the date
     * @return LocalDateTime at 00:00:00
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }
    
    /**
     * Gets the end of the day for a given date.
     * 
     * @param date the date
     * @return LocalDateTime at 23:59:59.999999999
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }
    
    /**
     * Gets the start of the current week (Monday).
     * 
     * @return LocalDate of the current week's Monday
     */
    public static LocalDate getStartOfWeek() {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue() - 1);
    }
    
    /**
     * Gets the end of the current week (Sunday).
     * 
     * @return LocalDate of the current week's Sunday
     */
    public static LocalDate getEndOfWeek() {
        return getStartOfWeek().plusDays(6);
    }
    
    /**
     * Gets the start of the current month.
     * 
     * @return LocalDate of the first day of the current month
     */
    public static LocalDate getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }
    
    /**
     * Gets the end of the current month.
     * 
     * @return LocalDate of the last day of the current month
     */
    public static LocalDate getEndOfMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
    
    /**
     * Gets the start of the current year.
     * 
     * @return LocalDate of January 1st of the current year
     */
    public static LocalDate getStartOfYear() {
        return LocalDate.now().withDayOfYear(1);
    }
    
    /**
     * Gets the end of the current year.
     * 
     * @return LocalDate of December 31st of the current year
     */
    public static LocalDate getEndOfYear() {
        LocalDate now = LocalDate.now();
        return now.withDayOfYear(now.lengthOfYear());
    }
    
    /**
     * Generates a list of available appointment slots for a given date.
     * 
     * @param date the date to generate slots for
     * @param slotDurationMinutes duration of each slot in minutes
     * @return list of available appointment times
     */
    public static List<LocalDateTime> generateAppointmentSlots(LocalDate date, int slotDurationMinutes) {
        List<LocalDateTime> slots = new ArrayList<>();
        
        if (date == null || !isWeekday(date) || isPastDate(date)) {
            return slots; // Return empty list for invalid dates
        }
        
        LocalTime startTime = LocalTime.of(Constants.CLINIC_START_HOUR, 0);
        LocalTime endTime = LocalTime.of(Constants.CLINIC_END_HOUR, 0);
        
        LocalDateTime currentSlot = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
        
        while (currentSlot.isBefore(endDateTime)) {
            slots.add(currentSlot);
            currentSlot = currentSlot.plusMinutes(slotDurationMinutes);
        }
        
        return slots;
    }
    
    /**
     * Finds the next available weekday from a given date.
     * 
     * @param fromDate the starting date
     * @return the next weekday
     */
    public static LocalDate getNextWeekday(LocalDate fromDate) {
        if (fromDate == null) {
            return null;
        }
        
        LocalDate nextDay = fromDate.plusDays(1);
        while (isWeekend(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        
        return nextDay;
    }
    
    /**
     * Finds the previous weekday from a given date.
     * 
     * @param fromDate the starting date
     * @return the previous weekday
     */
    public static LocalDate getPreviousWeekday(LocalDate fromDate) {
        if (fromDate == null) {
            return null;
        }
        
        LocalDate prevDay = fromDate.minusDays(1);
        while (isWeekend(prevDay)) {
            prevDay = prevDay.minusDays(1);
        }
        
        return prevDay;
    }
    
    // Appointment-specific utility methods
    
    /**
     * Calculates the expected end time of an appointment.
     * 
     * @param startTime the appointment start time
     * @param durationMinutes the duration in minutes
     * @return the expected end time
     */
    public static LocalDateTime calculateAppointmentEndTime(LocalDateTime startTime, int durationMinutes) {
        return startTime != null ? startTime.plusMinutes(durationMinutes) : null;
    }
    
    /**
     * Checks if two appointment time slots overlap.
     * 
     * @param start1 start time of first appointment
     * @param end1 end time of first appointment
     * @param start2 start time of second appointment
     * @param end2 end time of second appointment
     * @return true if appointments overlap
     */
    public static boolean doAppointmentsOverlap(LocalDateTime start1, LocalDateTime end1,
                                               LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
    
    /**
     * Calculates the buffer time needed between appointments.
     * 
     * @param appointmentDuration duration of the appointment in minutes
     * @return buffer time in minutes
     */
    public static int calculateBufferTime(int appointmentDuration) {
        // Standard buffer is 15 minutes, but longer for longer appointments
        if (appointmentDuration <= 30) {
            return 15;
        } else if (appointmentDuration <= 60) {
            return 30;
        } else {
            return 45;
        }
    }
    
    /**
     * Gets a human-readable relative time description.
     * 
     * @param dateTime the datetime to describe
     * @return relative time description (e.g., "2 hours ago", "in 3 days")
     */
    public static String getRelativeTimeDescription(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        if (duration.isNegative()) {
            // Future time
            duration = duration.abs();
            long minutes = duration.toMinutes();
            
            if (minutes < 60) {
                return "in " + minutes + " minute" + (minutes == 1 ? "" : "s");
            } else if (minutes < 1440) { // Less than 24 hours
                long hours = duration.toHours();
                return "in " + hours + " hour" + (hours == 1 ? "" : "s");
            } else {
                long days = duration.toDays();
                return "in " + days + " day" + (days == 1 ? "" : "s");
            }
        } else {
            // Past time
            long minutes = duration.toMinutes();
            
            if (minutes < 60) {
                return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
            } else if (minutes < 1440) { // Less than 24 hours
                long hours = duration.toHours();
                return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
            } else {
                long days = duration.toDays();
                return days + " day" + (days == 1 ? "" : "s") + " ago";
            }
        }
    }
    
    /**
     * Formats a duration in a human-readable format.
     * 
     * @param durationMinutes duration in minutes
     * @return formatted duration string
     */
    public static String formatDuration(long durationMinutes) {
        if (durationMinutes < 60) {
            return durationMinutes + " minute" + (durationMinutes == 1 ? "" : "s");
        } else if (durationMinutes < 1440) { // Less than 24 hours
            long hours = durationMinutes / 60;
            long remainingMinutes = durationMinutes % 60;
            
            String result = hours + " hour" + (hours == 1 ? "" : "s");
            if (remainingMinutes > 0) {
                result += " " + remainingMinutes + " minute" + (remainingMinutes == 1 ? "" : "s");
            }
            return result;
        } else {
            long days = durationMinutes / 1440;
            long remainingHours = (durationMinutes % 1440) / 60;
            
            String result = days + " day" + (days == 1 ? "" : "s");
            if (remainingHours > 0) {
                result += " " + remainingHours + " hour" + (remainingHours == 1 ? "" : "s");
            }
            return result;
        }
    }
}