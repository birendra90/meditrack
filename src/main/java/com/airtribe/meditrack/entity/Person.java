package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.interfaces.Searchable;
import com.airtribe.meditrack.constants.Constants;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract base class representing a person in the medical system.
 * This demonstrates inheritance, encapsulation, and polymorphism.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public abstract class Person extends MedicalEntity implements Searchable<Person> {
    
    private static final long serialVersionUID = 1L;
    
    // Private fields demonstrating encapsulation
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String bloodGroup;
    
    /**
     * Default constructor.
     */
    protected Person() {
        super();
    }
    
    /**
     * Constructor with basic information.
     * 
     * @param id the unique identifier
     * @param firstName the first name
     * @param lastName the last name
     * @param dateOfBirth the date of birth
     * @param gender the gender
     */
    protected Person(String id, String firstName, String lastName, LocalDate dateOfBirth, String gender) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }
    
    /**
     * Constructor with complete information.
     */
    protected Person(String id, String firstName, String lastName, LocalDate dateOfBirth, 
                   String gender, String email, String phone, String address) {
        this(id, firstName, lastName, dateOfBirth, gender);
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    // Getters and Setters demonstrating encapsulation with validation
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateTimestamp();
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateTimestamp();
    }
    
    /**
     * Gets the full name by combining first and last names.
     * 
     * @return the full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "Unknown";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        updateTimestamp();
    }
    
    /**
     * Calculates and returns the current age.
     * 
     * @return the age in years
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
        updateTimestamp();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
        updateTimestamp();
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
        updateTimestamp();
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
        updateTimestamp();
    }
    
    public String getEmergencyPhone() {
        return emergencyPhone;
    }
    
    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
        updateTimestamp();
    }
    
    public String getBloodGroup() {
        return bloodGroup;
    }
    
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
        updateTimestamp();
    }
    
    // Implementation of Searchable interface
    
    @Override
    public String getSearchId() {
        return getId();
    }
    
    @Override
    public String getPrimarySearchTerm() {
        return getFullName();
    }
    
    @Override
    public List<String> getSearchableTerms() {
        List<String> terms = new ArrayList<>();
        
        if (getId() != null) terms.add(getId());
        if (firstName != null) terms.add(firstName);
        if (lastName != null) terms.add(lastName);
        if (getFullName() != null) terms.add(getFullName());
        if (email != null) terms.add(email);
        if (phone != null) terms.add(phone);
        
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
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.add("First name is required");
        } else if (firstName.length() < Constants.MIN_NAME_LENGTH || firstName.length() > Constants.MAX_NAME_LENGTH) {
            errors.add("First name must be between " + Constants.MIN_NAME_LENGTH + 
                      " and " + Constants.MAX_NAME_LENGTH + " characters");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            errors.add("Last name is required");
        } else if (lastName.length() < Constants.MIN_NAME_LENGTH || lastName.length() > Constants.MAX_NAME_LENGTH) {
            errors.add("Last name must be between " + Constants.MIN_NAME_LENGTH + 
                      " and " + Constants.MAX_NAME_LENGTH + " characters");
        }
        
        if (dateOfBirth == null) {
            errors.add("Date of birth is required");
        } else {
            int age = getAge();
            if (age < Constants.MIN_AGE || age > Constants.MAX_AGE) {
                errors.add("Age must be between " + Constants.MIN_AGE + " and " + Constants.MAX_AGE);
            }
            if (dateOfBirth.isAfter(LocalDate.now())) {
                errors.add("Date of birth cannot be in the future");
            }
        }
        
        if (gender == null || gender.trim().isEmpty()) {
            errors.add("Gender is required");
        }
        
        // Optional field validation
        if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
            errors.add("Invalid email format");
        }
        
        if (phone != null && !phone.trim().isEmpty() && !isValidPhone(phone)) {
            errors.add("Invalid phone number format");
        }
        
        if (emergencyPhone != null && !emergencyPhone.trim().isEmpty() && !isValidPhone(emergencyPhone)) {
            errors.add("Invalid emergency phone number format");
        }
        
        return errors.toArray(new String[0]);
    }
    
    /**
     * Validates email format using simple regex.
     * 
     * @param email the email to validate
     * @return true if valid email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validates phone number format.
     * 
     * @param phone the phone number to validate
     * @return true if valid phone format
     */
    private boolean isValidPhone(String phone) {
        // Remove all non-digit characters
        String cleanPhone = phone.replaceAll("\\D", "");
        return cleanPhone.length() >= Constants.MIN_PHONE_LENGTH && 
               cleanPhone.length() <= Constants.MAX_PHONE_LENGTH;
    }
    
    @Override
    public String getDisplayName() {
        return getFullName();
    }
    
    @Override
    public String getEntityType() {
        return "Person";
    }
    
    /**
     * Checks if this person is a senior citizen.
     * 
     * @return true if age >= 65
     */
    public boolean isSeniorCitizen() {
        return getAge() >= 65;
    }
    
    /**
     * Checks if this person is a minor.
     * 
     * @return true if age < 18
     */
    public boolean isMinor() {
        return getAge() < 18;
    }
    
    /**
     * Gets the age category as string.
     * 
     * @return age category (Child, Adult, Senior)
     */
    public String getAgeCategory() {
        int age = getAge();
        if (age < 18) {
            return "Child";
        } else if (age >= 65) {
            return "Senior";
        } else {
            return "Adult";
        }
    }
    
    /**
     * Gets formatted contact information.
     * 
     * @return formatted contact string
     */
    public String getContactInfo() {
        StringBuilder contact = new StringBuilder();
        
        if (phone != null && !phone.trim().isEmpty()) {
            contact.append("Phone: ").append(phone);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            if (contact.length() > 0) contact.append(", ");
            contact.append("Email: ").append(email);
        }
        
        return contact.toString();
    }
    
    /**
     * Gets formatted emergency contact information.
     * 
     * @return formatted emergency contact string
     */
    public String getEmergencyContactInfo() {
        StringBuilder emergency = new StringBuilder();
        
        if (emergencyContact != null && !emergencyContact.trim().isEmpty()) {
            emergency.append("Contact: ").append(emergencyContact);
        }
        
        if (emergencyPhone != null && !emergencyPhone.trim().isEmpty()) {
            if (emergency.length() > 0) emergency.append(", ");
            emergency.append("Phone: ").append(emergencyPhone);
        }
        
        return emergency.toString();
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', age=%d, gender='%s'}", 
                getClass().getSimpleName(), getId(), getFullName(), getAge(), gender);
    }
    
    /**
     * Gets a detailed string representation.
     * 
     * @return detailed string representation
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFullName()).append(" (").append(getAge()).append(" years)\n");
        sb.append("ID: ").append(getId()).append("\n");
        sb.append("Gender: ").append(gender).append("\n");
        sb.append("DOB: ").append(dateOfBirth).append("\n");
        
        if (phone != null) sb.append("Phone: ").append(phone).append("\n");
        if (email != null) sb.append("Email: ").append(email).append("\n");
        if (address != null) sb.append("Address: ").append(address).append("\n");
        if (bloodGroup != null) sb.append("Blood Group: ").append(bloodGroup).append("\n");
        
        return sb.toString();
    }
}