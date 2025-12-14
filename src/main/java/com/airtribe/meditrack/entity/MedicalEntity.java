package com.airtribe.meditrack.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Abstract base class for all medical entities in the system.
 * This demonstrates abstraction and provides common functionality.
 * 
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public abstract class MedicalEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    protected String id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected boolean active;
    
    /**
     * Default constructor.
     */
    protected MedicalEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }
    
    /**
     * Constructor with ID.
     * 
     * @param id the unique identifier for this entity
     */
    protected MedicalEntity(String id) {
        this();
        this.id = id;
    }
    
    /**
     * Gets the unique identifier for this entity.
     * 
     * @return the unique identifier
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the unique identifier for this entity.
     * 
     * @param id the unique identifier
     */
    public void setId(String id) {
        this.id = id;
        updateTimestamp();
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     * 
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Checks if this entity is active.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Sets the active status of this entity.
     * 
     * @param active the active status
     */
    public void setActive(boolean active) {
        this.active = active;
        updateTimestamp();
    }
    
    /**
     * Updates the last modified timestamp.
     */
    protected void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Deactivates this entity (soft delete).
     */
    public void deactivate() {
        setActive(false);
    }
    
    /**
     * Reactivates this entity.
     */
    public void reactivate() {
        setActive(true);
    }
    
    /**
     * Abstract method to validate the entity.
     * Subclasses must implement their own validation logic.
     * 
     * @return true if the entity is valid, false otherwise
     */
    public abstract boolean isValid();
    
    /**
     * Abstract method to get validation error messages.
     * Subclasses must implement their own validation error reporting.
     * 
     * @return array of validation error messages, empty if valid
     */
    public abstract String[] getValidationErrors();
    
    /**
     * Abstract method to get a display name for this entity.
     * 
     * @return display name for this entity
     */
    public abstract String getDisplayName();
    
    /**
     * Abstract method to get entity type.
     * 
     * @return the type of this entity
     */
    public abstract String getEntityType();
    
    /**
     * Gets a summary of this entity for logging or display purposes.
     * 
     * @return entity summary
     */
    public String getSummary() {
        return String.format("%s [ID: %s, Type: %s, Active: %s, Created: %s]",
                getDisplayName(),
                getId(),
                getEntityType(),
                isActive(),
                getCreatedAt().toLocalDate());
    }
    
    /**
     * Checks if this entity was created today.
     * 
     * @return true if created today, false otherwise
     */
    public boolean isCreatedToday() {
        return createdAt.toLocalDate().equals(java.time.LocalDate.now());
    }
    
    /**
     * Checks if this entity was updated today.
     * 
     * @return true if updated today, false otherwise
     */
    public boolean isUpdatedToday() {
        return updatedAt.toLocalDate().equals(java.time.LocalDate.now());
    }
    
    /**
     * Gets the age of this entity in days.
     * 
     * @return age in days
     */
    public long getAgeInDays() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }
    
    /**
     * Template method for performing validation and throwing exception if invalid.
     * This demonstrates the Template Method pattern.
     * 
     * @throws com.airtribe.meditrack.exception.InvalidDataException if entity is invalid
     */
    public final void validateAndThrow() throws com.airtribe.meditrack.exception.InvalidDataException {
        if (!isValid()) {
            String[] errors = getValidationErrors();
            String combinedErrors = String.join(", ", errors);
            throw new com.airtribe.meditrack.exception.InvalidDataException(
                    "Validation failed for " + getEntityType() + ": " + combinedErrors,
                    getEntityType(),
                    this,
                    combinedErrors
            );
        }
    }
    
    /**
     * Template method for entity initialization.
     * Subclasses can override to provide custom initialization logic.
     */
    protected void initialize() {
        // Default implementation does nothing
        // Subclasses can override for custom initialization
    }
    
    /**
     * Template method for entity cleanup.
     * Subclasses can override to provide custom cleanup logic.
     */
    protected void cleanup() {
        // Default implementation does nothing
        // Subclasses can override for custom cleanup
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MedicalEntity that = (MedicalEntity) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', active=%s}", 
                getClass().getSimpleName(), id, active);
    }
}