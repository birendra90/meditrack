package com.airtribe.meditrack.interfaces;

import java.util.List;

/**
 * Interface for entities that can be searched.
 * This demonstrates interface design with default methods.
 * 
 * @param <T> The type of entity that implements this interface
 * @author MediTrack Team
 * @version 1.0
 * @since 1.0
 */
public interface Searchable<T> {
    
    /**
     * Get the unique identifier for search purposes.
     * 
     * @return the unique identifier
     */
    String getSearchId();
    
    /**
     * Get the primary search term (usually name).
     * 
     * @return the primary search term
     */
    String getPrimarySearchTerm();
    
    /**
     * Get all searchable terms for this entity.
     * 
     * @return list of searchable terms
     */
    List<String> getSearchableTerms();
    
    /**
     * Check if this entity matches the given search criteria.
     * 
     * @param criteria the search criteria
     * @param caseSensitive whether the search should be case sensitive
     * @return true if this entity matches the criteria
     */
    boolean matches(String criteria, boolean caseSensitive);
    
    /**
     * Default implementation for case-insensitive search.
     * 
     * @param criteria the search criteria
     * @return true if this entity matches the criteria (case-insensitive)
     */
    default boolean matches(String criteria) {
        return matches(criteria, false);
    }
    
    /**
     * Default implementation to check if any searchable term contains the criteria.
     * 
     * @param criteria the search criteria
     * @param caseSensitive whether the search should be case sensitive
     * @return true if any searchable term contains the criteria
     */
    default boolean matchesAny(String criteria, boolean caseSensitive) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return false;
        }
        
        String searchTerm = caseSensitive ? criteria : criteria.toLowerCase();
        
        for (String term : getSearchableTerms()) {
            if (term != null) {
                String compareTerm = caseSensitive ? term : term.toLowerCase();
                if (compareTerm.contains(searchTerm)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Default implementation for case-insensitive partial matching.
     * 
     * @param criteria the search criteria
     * @return true if any searchable term contains the criteria (case-insensitive)
     */
    default boolean matchesAny(String criteria) {
        return matchesAny(criteria, false);
    }
    
    /**
     * Default implementation to check if any searchable term starts with the criteria.
     * 
     * @param criteria the search criteria
     * @param caseSensitive whether the search should be case sensitive
     * @return true if any searchable term starts with the criteria
     */
    default boolean startsWith(String criteria, boolean caseSensitive) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return false;
        }
        
        String searchTerm = caseSensitive ? criteria : criteria.toLowerCase();
        
        for (String term : getSearchableTerms()) {
            if (term != null) {
                String compareTerm = caseSensitive ? term : term.toLowerCase();
                if (compareTerm.startsWith(searchTerm)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Default implementation for case-insensitive prefix matching.
     * 
     * @param criteria the search criteria
     * @return true if any searchable term starts with the criteria (case-insensitive)
     */
    default boolean startsWith(String criteria) {
        return startsWith(criteria, false);
    }
    
    /**
     * Default implementation to calculate search relevance score.
     * Higher score indicates better match.
     * 
     * @param criteria the search criteria
     * @return relevance score (0.0 to 1.0)
     */
    default double getSearchRelevanceScore(String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return 0.0;
        }
        
        String searchTerm = criteria.toLowerCase();
        double maxScore = 0.0;
        
        for (String term : getSearchableTerms()) {
            if (term != null) {
                String compareTerm = term.toLowerCase();
                double score = calculateTermRelevance(compareTerm, searchTerm);
                maxScore = Math.max(maxScore, score);
            }
        }
        
        return maxScore;
    }
    
    /**
     * Helper method to calculate relevance between two terms.
     * 
     * @param term the term from the entity
     * @param searchTerm the search criteria
     * @return relevance score (0.0 to 1.0)
     */
    private double calculateTermRelevance(String term, String searchTerm) {
        if (term.equals(searchTerm)) {
            return 1.0; // Exact match
        } else if (term.startsWith(searchTerm)) {
            return 0.9; // Starts with
        } else if (term.contains(searchTerm)) {
            return 0.7; // Contains
        } else if (searchTerm.length() > 3 && term.contains(searchTerm.substring(0, 3))) {
            return 0.3; // Partial match
        } else {
            return 0.0; // No match
        }
    }
    
    /**
     * Default implementation for fuzzy search using simple character matching.
     * 
     * @param criteria the search criteria
     * @param threshold minimum similarity threshold (0.0 to 1.0)
     * @return true if similarity exceeds threshold
     */
    default boolean fuzzyMatches(String criteria, double threshold) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return false;
        }
        
        for (String term : getSearchableTerms()) {
            if (term != null) {
                double similarity = calculateSimilarity(term.toLowerCase(), criteria.toLowerCase());
                if (similarity >= threshold) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Helper method to calculate simple similarity between two strings.
     * Uses a basic character-based approach.
     * 
     * @param str1 first string
     * @param str2 second string
     * @return similarity score (0.0 to 1.0)
     */
    private double calculateSimilarity(String str1, String str2) {
        if (str1.equals(str2)) return 1.0;
        
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) return 1.0;
        
        int commonChars = 0;
        int minLength = Math.min(str1.length(), str2.length());
        
        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                commonChars++;
            }
        }
        
        return (double) commonChars / maxLength;
    }
    
    /**
     * Get a display-friendly representation for search results.
     * 
     * @return display string for search results
     */
    default String getSearchDisplayString() {
        return getPrimarySearchTerm() + " (ID: " + getSearchId() + ")";
    }
}