package com.airtribe.meditrack.util;

import com.airtribe.meditrack.interfaces.Searchable;
import com.airtribe.meditrack.exception.InvalidDataException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic data store utility class demonstrating generics, collections, and thread safety.
 * This class provides in-memory storage with CRUD operations, search capabilities, and thread safety.
 * 
 * @param <T> The type of entities stored in this data store
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public class DataStore<T> {
    
    // Thread-safe storage using ConcurrentHashMap
    private final Map<String, T> storage;
    
    // Read-write lock for enhanced thread safety on complex operations
    private final ReadWriteLock lock;
    
    // Optional comparator for sorting
    private Comparator<T> defaultComparator;
    
    // Store metadata
    private final String storeType;
    private final long createdAt;
    private volatile long lastModified;
    private volatile int operationCount;
    
    /**
     * Default constructor.
     */
    public DataStore() {
        this("Generic");
    }
    
    /**
     * Constructor with store type identifier.
     * 
     * @param storeType the type identifier for this store
     */
    public DataStore(String storeType) {
        this.storage = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.storeType = storeType;
        this.createdAt = System.currentTimeMillis();
        this.lastModified = this.createdAt;
        this.operationCount = 0;
    }
    
    /**
     * Constructor with initial capacity.
     * 
     * @param storeType the type identifier for this store
     * @param initialCapacity the initial capacity of the underlying map
     */
    public DataStore(String storeType, int initialCapacity) {
        this.storage = new ConcurrentHashMap<>(initialCapacity);
        this.lock = new ReentrantReadWriteLock();
        this.storeType = storeType;
        this.createdAt = System.currentTimeMillis();
        this.lastModified = this.createdAt;
        this.operationCount = 0;
    }
    
    /**
     * Sets the default comparator for sorting operations.
     * 
     * @param comparator the comparator to use
     */
    public void setDefaultComparator(Comparator<T> comparator) {
        this.defaultComparator = comparator;
    }
    
    // CRUD Operations
    
    /**
     * Stores an entity with the given key.
     * 
     * @param key the unique key for the entity
     * @param entity the entity to store
     * @return the previous entity associated with the key, or null
     * @throws IllegalArgumentException if key or entity is null
     */
    public T store(String key, T entity) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            T previous = storage.put(key, entity);
            updateMetadata();
            return previous;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Retrieves an entity by its key.
     * 
     * @param key the key to look up
     * @return the entity, or null if not found
     */
    public T get(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        lock.readLock().lock();
        try {
            return storage.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retrieves an entity by key with a default value.
     * 
     * @param key the key to look up
     * @param defaultValue the default value if key is not found
     * @return the entity or default value
     */
    public T getOrDefault(String key, T defaultValue) {
        if (key == null || key.trim().isEmpty()) {
            return defaultValue;
        }
        
        lock.readLock().lock();
        try {
            return storage.getOrDefault(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Updates an existing entity.
     * 
     * @param key the key of the entity to update
     * @param entity the updated entity
     * @return true if entity was updated, false if key didn't exist
     * @throws IllegalArgumentException if key or entity is null
     */
    public boolean update(String key, T entity) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            if (storage.containsKey(key)) {
                storage.put(key, entity);
                updateMetadata();
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Removes an entity by its key.
     * 
     * @param key the key of the entity to remove
     * @return the removed entity, or null if not found
     */
    public T remove(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        lock.writeLock().lock();
        try {
            T removed = storage.remove(key);
            if (removed != null) {
                updateMetadata();
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Checks if the store contains an entity with the given key.
     * 
     * @param key the key to check
     * @return true if key exists, false otherwise
     */
    public boolean contains(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        lock.readLock().lock();
        try {
            return storage.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets the number of entities in the store.
     * 
     * @return the size of the store
     */
    public int size() {
        lock.readLock().lock();
        try {
            return storage.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Checks if the store is empty.
     * 
     * @return true if store is empty, false otherwise
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return storage.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Clears all entities from the store.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            storage.clear();
            updateMetadata();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Bulk Operations
    
    /**
     * Stores multiple entities at once.
     * 
     * @param entities map of key-entity pairs to store
     * @throws IllegalArgumentException if entities map is null or contains null values
     */
    public void storeAll(Map<String, T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Entities map cannot be null");
        }
        
        // Validate all entries first
        for (Map.Entry<String, T> entry : entities.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException("Key cannot be null or empty");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Entity cannot be null");
            }
        }
        
        lock.writeLock().lock();
        try {
            storage.putAll(entities);
            updateMetadata();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Removes multiple entities by their keys.
     * 
     * @param keys the keys of entities to remove
     * @return list of removed entities
     */
    public List<T> removeAll(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<T> removed = new ArrayList<>();
        lock.writeLock().lock();
        try {
            for (String key : keys) {
                T entity = storage.remove(key);
                if (entity != null) {
                    removed.add(entity);
                }
            }
            if (!removed.isEmpty()) {
                updateMetadata();
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Query Operations
    
    /**
     * Gets all entities in the store.
     * 
     * @return list of all entities (defensive copy)
     */
    public List<T> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(storage.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets all keys in the store.
     * 
     * @return set of all keys (defensive copy)
     */
    public Set<String> getAllKeys() {
        lock.readLock().lock();
        try {
            return new HashSet<>(storage.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Gets all entities sorted by the default comparator.
     * 
     * @return sorted list of entities
     * @throws IllegalStateException if no default comparator is set
     */
    public List<T> getAllSorted() {
        if (defaultComparator == null) {
            throw new IllegalStateException("No default comparator set");
        }
        return getAllSorted(defaultComparator);
    }
    
    /**
     * Gets all entities sorted by the given comparator.
     * 
     * @param comparator the comparator to use for sorting
     * @return sorted list of entities
     */
    public List<T> getAllSorted(Comparator<T> comparator) {
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Finds entities that match the given predicate.
     * 
     * @param predicate the condition to match
     * @return list of matching entities
     */
    public List<T> findWhere(Predicate<T> predicate) {
        if (predicate == null) {
            return getAll();
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Finds the first entity that matches the given predicate.
     * 
     * @param predicate the condition to match
     * @return the first matching entity, or null if none found
     */
    public T findFirst(Predicate<T> predicate) {
        if (predicate == null) {
            return null;
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .filter(predicate)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Counts entities that match the given predicate.
     * 
     * @param predicate the condition to match
     * @return count of matching entities
     */
    public long count(Predicate<T> predicate) {
        if (predicate == null) {
            return size();
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .filter(predicate)
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Checks if any entity matches the given predicate.
     * 
     * @param predicate the condition to match
     * @return true if any entity matches, false otherwise
     */
    public boolean anyMatch(Predicate<T> predicate) {
        if (predicate == null) {
            return !isEmpty();
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .anyMatch(predicate);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Checks if all entities match the given predicate.
     * 
     * @param predicate the condition to match
     * @return true if all entities match, false otherwise
     */
    public boolean allMatch(Predicate<T> predicate) {
        if (predicate == null) {
            return true;
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .allMatch(predicate);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Search Operations (for Searchable entities)
    
    /**
     * Searches for entities using the Searchable interface (if implemented).
     * 
     * @param criteria the search criteria
     * @return list of matching entities
     */
    @SuppressWarnings("unchecked")
    public List<T> search(String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return getAll();
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .filter(entity -> {
                        if (entity instanceof Searchable) {
                            return ((Searchable<T>) entity).matches(criteria);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Searches for entities and sorts by relevance score (if Searchable).
     * 
     * @param criteria the search criteria
     * @return list of matching entities sorted by relevance
     */
    @SuppressWarnings("unchecked")
    public List<T> searchAndSort(String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return getAll();
        }
        
        lock.readLock().lock();
        try {
            return storage.values().stream()
                    .filter(entity -> {
                        if (entity instanceof Searchable) {
                            return ((Searchable<T>) entity).matches(criteria);
                        }
                        return false;
                    })
                    .sorted((e1, e2) -> {
                        if (e1 instanceof Searchable && e2 instanceof Searchable) {
                            double score1 = ((Searchable<T>) e1).getSearchRelevanceScore(criteria);
                            double score2 = ((Searchable<T>) e2).getSearchRelevanceScore(criteria);
                            return Double.compare(score2, score1); // Higher scores first
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Pagination
    
    /**
     * Gets a page of entities.
     * 
     * @param pageNumber the page number (0-based)
     * @param pageSize the size of each page
     * @return page of entities
     */
    public Page<T> getPage(int pageNumber, int pageSize) {
        return getPage(pageNumber, pageSize, null);
    }
    
    /**
     * Gets a page of entities with optional comparator.
     * 
     * @param pageNumber the page number (0-based)
     * @param pageSize the size of each page
     * @param comparator optional comparator for sorting
     * @return page of entities
     */
    public Page<T> getPage(int pageNumber, int pageSize, Comparator<T> comparator) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid page parameters");
        }
        
        lock.readLock().lock();
        try {
            List<T> allEntities = comparator != null ? 
                    getAllSorted(comparator) : 
                    getAll();
            
            int totalElements = allEntities.size();
            int totalPages = (totalElements + pageSize - 1) / pageSize;
            
            int startIndex = pageNumber * pageSize;
            if (startIndex >= totalElements) {
                return new Page<>(Collections.emptyList(), pageNumber, pageSize, totalElements, totalPages);
            }
            
            int endIndex = Math.min(startIndex + pageSize, totalElements);
            List<T> pageContent = allEntities.subList(startIndex, endIndex);
            
            return new Page<>(pageContent, pageNumber, pageSize, totalElements, totalPages);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Utility Methods
    
    /**
     * Updates the metadata after a modification operation.
     */
    private void updateMetadata() {
        this.lastModified = System.currentTimeMillis();
        this.operationCount++;
    }
    
    /**
     * Gets store statistics.
     * 
     * @return formatted statistics string
     */
    public String getStatistics() {
        lock.readLock().lock();
        try {
            long now = System.currentTimeMillis();
            long ageMs = now - createdAt;
            long lastModifiedAge = now - lastModified;
            
            return String.format(
                "DataStore Statistics [%s]:\n" +
                "  Size: %d entities\n" +
                "  Age: %d ms\n" +
                "  Last Modified: %d ms ago\n" +
                "  Operations: %d\n" +
                "  Memory Usage: ~%d bytes",
                storeType, storage.size(), ageMs, lastModifiedAge, operationCount,
                estimateMemoryUsage()
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Estimates memory usage (rough calculation).
     * 
     * @return estimated memory usage in bytes
     */
    private long estimateMemoryUsage() {
        // Rough estimation: each entry overhead + string keys + object references
        return storage.size() * 100L; // Very rough estimate
    }
    
    /**
     * Validates the internal consistency of the store.
     * 
     * @return validation results
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        lock.readLock().lock();
        try {
            // Check for null values
            for (Map.Entry<String, T> entry : storage.entrySet()) {
                if (entry.getKey() == null) {
                    result.addError("Found null key in storage");
                }
                if (entry.getValue() == null) {
                    result.addError("Found null value for key: " + entry.getKey());
                }
            }
            
            // Additional validation can be added here
            result.setEntityCount(storage.size());
            
        } finally {
            lock.readLock().unlock();
        }
        
        return result;
    }
    
    /**
     * Creates a snapshot of the current store state.
     * 
     * @return snapshot of the store
     */
    public DataStoreSnapshot<T> createSnapshot() {
        lock.readLock().lock();
        try {
            Map<String, T> data = new HashMap<>(storage);
            return new DataStoreSnapshot<>(storeType, data, System.currentTimeMillis());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Restores the store from a snapshot.
     * 
     * @param snapshot the snapshot to restore from
     */
    public void restoreFromSnapshot(DataStoreSnapshot<T> snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            storage.clear();
            storage.putAll(snapshot.getData());
            updateMetadata();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Nested Classes
    
    /**
     * Represents a page of entities for pagination.
     * 
     * @param <T> the entity type
     */
    public static class Page<T> {
        private final List<T> content;
        private final int pageNumber;
        private final int pageSize;
        private final int totalElements;
        private final int totalPages;
        
        public Page(List<T> content, int pageNumber, int pageSize, int totalElements, int totalPages) {
            this.content = Collections.unmodifiableList(new ArrayList<>(content));
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
        
        public List<T> getContent() { return content; }
        public int getPageNumber() { return pageNumber; }
        public int getPageSize() { return pageSize; }
        public int getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean hasNext() { return pageNumber < totalPages - 1; }
        public boolean hasPrevious() { return pageNumber > 0; }
        public boolean isFirst() { return pageNumber == 0; }
        public boolean isLast() { return pageNumber == totalPages - 1; }
        
        @Override
        public String toString() {
            return String.format("Page %d of %d (showing %d of %d total items)",
                    pageNumber + 1, totalPages, content.size(), totalElements);
        }
    }
    
    /**
     * Snapshot of store state for backup/restore operations.
     * 
     * @param <T> the entity type
     */
    public static class DataStoreSnapshot<T> {
        private final String storeType;
        private final Map<String, T> data;
        private final long timestamp;
        
        public DataStoreSnapshot(String storeType, Map<String, T> data, long timestamp) {
            this.storeType = storeType;
            this.data = Collections.unmodifiableMap(new HashMap<>(data));
            this.timestamp = timestamp;
        }
        
        public String getStoreType() { return storeType; }
        public Map<String, T> getData() { return data; }
        public long getTimestamp() { return timestamp; }
        
        public java.time.LocalDateTime getCreatedAt() {
            return java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    java.time.ZoneId.systemDefault()
            );
        }
        
        @Override
        public String toString() {
            return String.format("Snapshot of %s store (%d entities) created at %s",
                    storeType, data.size(), getCreatedAt());
        }
    }
    
    /**
     * Validation result class.
     */
    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private int entityCount = 0;
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void setEntityCount(int count) { this.entityCount = count; }
        
        public boolean isValid() { return errors.isEmpty(); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public int getEntityCount() { return entityCount; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result: ").append(isValid() ? "VALID" : "INVALID").append("\n");
            sb.append("Entity Count: ").append(entityCount).append("\n");
            
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                errors.forEach(error -> sb.append("  - ").append(error).append("\n"));
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                warnings.forEach(warning -> sb.append("  - ").append(warning).append("\n"));
            }
            
            return sb.toString();
        }
    }
    
    @Override
    public String toString() {
        return String.format("DataStore[%s]: %d entities", storeType, size());
    }
}