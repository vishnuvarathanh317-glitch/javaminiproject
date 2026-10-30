package com.hostelmanagement.model;

import com.hostelmanagement.enums.ComplaintPriority;

/**
 * AI-generated complaint priority recommendation with explainable scoring.
 * Used by the Complaint Priority Recommendation Engine.
 */
public class ComplaintPriorityRecommendation {
    private final ComplaintPriority recommendedPriority;
    private final double score;
    private final String explanation;

    public ComplaintPriorityRecommendation(ComplaintPriority priority,
                                           double score, String explanation) {
        this.recommendedPriority = priority;
        this.score = score;
        this.explanation = explanation;
    }

    public ComplaintPriority getRecommendedPriority() { return recommendedPriority; }
    public ComplaintPriority getPriority() { return recommendedPriority; }
    public double getScore() { return score; }
    public String getExplanation() { return explanation; }

    @Override
    public String toString() {
        return String.format("Priority: %s (Score: %.1f) — %s",
                recommendedPriority, score, explanation);
    }
}
