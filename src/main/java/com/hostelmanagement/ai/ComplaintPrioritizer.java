package com.hostelmanagement.ai;

import com.hostelmanagement.enums.ComplaintCategory;
import com.hostelmanagement.enums.ComplaintPriority;
import com.hostelmanagement.model.ComplaintPriorityRecommendation;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent Complaint Prioritization Engine (Rule-based AI).
 *
 * Calculates a multi-factor urgency and impact score to recommend a priority
 * level with explainable justification.
 */
public class ComplaintPrioritizer {

    /**
     * Recommends a ComplaintPriority with explainable score breakdown.
     *
     * @param category     the complaint domain (e.g. Electricity, Water)
     * @param urgency      user-reported urgency scale (1 to 5)
     * @param impactCount  number of students affected (e.g., 1 for room bed, 10+ for entire floor/block)
     * @param waitingHours number of hours elapsed since complaint submission
     * @return recommendation with calculated priority, score, and explanation
     */
    public ComplaintPriorityRecommendation calculatePriority(
            ComplaintCategory category,
            int urgency,
            int impactCount,
            long waitingHours) {

        double score = 0.0;
        List<String> factors = new ArrayList<>();

        // Factor 1: Category base severity
        double categoryBase;
        if (category == ComplaintCategory.WATER_SUPPLY || category == ComplaintCategory.ELECTRICITY) {
            categoryBase = 35.0;
            factors.add("Critical utility infrastructure (" + category + ": +35)");
        } else if (category == ComplaintCategory.PLUMBING) {
            categoryBase = 25.0;
            factors.add("Sanitary & plumbing maintenance (" + category + ": +25)");
        } else if (category == ComplaintCategory.WIFI || category == ComplaintCategory.CLEANING) {
            categoryBase = 15.0;
            factors.add("General amenities (" + category + ": +15)");
        } else {
            categoryBase = 10.0;
            factors.add("Standard maintenance (" + (category != null ? category : "General") + ": +10)");
        }
        score += categoryBase;

        // Factor 2: Urgency level (1-5, each point adds 7, max 35)
        int clampedUrgency = Math.max(1, Math.min(5, urgency));
        double urgencyPoints = clampedUrgency * 7.0;
        score += urgencyPoints;
        factors.add("Reported urgency level " + clampedUrgency + "/5 (+" + (int)urgencyPoints + ")");

        // Factor 3: Impact / population affected (max 20 pts)
        int clampedImpact = Math.max(1, impactCount);
        double impactPoints = Math.min(20.0, clampedImpact * 4.0);
        score += impactPoints;
        if (clampedImpact > 1) {
            factors.add("Multiple students affected (" + clampedImpact + " students: +" + (int)impactPoints + ")");
        } else {
            factors.add("Individual impact (+4)");
        }

        // Factor 4: Aging backlog escalation (0.5 pts per hour waiting, max 15 pts)
        if (waitingHours > 0) {
            double agingPoints = Math.min(15.0, waitingHours * 0.5);
            score += agingPoints;
            factors.add("Pending backlog duration (" + waitingHours + " hrs: +" + String.format("%.1f", agingPoints) + ")");
        }

        // Threshold classification
        ComplaintPriority priority;
        if (score >= 70.0) {
            priority = ComplaintPriority.CRITICAL;
        } else if (score >= 50.0) {
            priority = ComplaintPriority.HIGH;
        } else if (score >= 30.0) {
            priority = ComplaintPriority.MEDIUM;
        } else {
            priority = ComplaintPriority.LOW;
        }

        String explanation = "Assigned " + priority + " priority (Score: " + String.format("%.1f", score) + "/100). "
                + "Factors: " + String.join(", ", factors) + ".";

        return new ComplaintPriorityRecommendation(priority, score, explanation);
    }
}
