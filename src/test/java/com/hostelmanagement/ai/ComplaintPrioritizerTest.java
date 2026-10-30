package com.hostelmanagement.ai;

import com.hostelmanagement.enums.ComplaintCategory;
import com.hostelmanagement.enums.ComplaintPriority;
import com.hostelmanagement.model.ComplaintPriorityRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Complaint Prioritizer (AI Algorithm & Multi-factor Evaluation)")
class ComplaintPrioritizerTest {

    private ComplaintPrioritizer prioritizer;

    @BeforeEach
    void setUp() {
        prioritizer = new ComplaintPrioritizer();
    }

    @Test
    @DisplayName("Critical emergency with widespread outage generates CRITICAL priority")
    void testCriticalPriority() {
        // Water supply (35 pts) + Urgency 5 (35 pts) + 10 people affected (20 pts) + 48 hrs waiting (15 pts max) = 105 -> score >= 70
        ComplaintPriorityRecommendation rec = prioritizer.calculatePriority(
                ComplaintCategory.WATER_SUPPLY, 5, 10, 48);

        assertEquals(ComplaintPriority.CRITICAL, rec.getPriority());
        assertTrue(rec.getScore() >= 70.0);
        assertTrue(rec.getExplanation().contains("Assigned CRITICAL priority"));
        assertTrue(rec.getExplanation().contains("Critical utility infrastructure"));
    }

    @Test
    @DisplayName("Minor furniture issue with 1 person generates LOW priority")
    void testLowPriority() {
        // Furniture (10 pts) + Urgency 1 (7 pts) + 1 person (4 pts) + 0 hrs waiting (0 pts) = 21 pts (< 30)
        ComplaintPriorityRecommendation rec = prioritizer.calculatePriority(
                ComplaintCategory.FURNITURE, 1, 1, 0);

        assertEquals(ComplaintPriority.LOW, rec.getPriority());
        assertTrue(rec.getScore() < 30.0);
        assertTrue(rec.getExplanation().contains("Assigned LOW priority"));
    }

    @Test
    @DisplayName("Aging backlog escalates priority over time")
    void testBacklogAgingEscalation() {
        // Fresh complaint: Plumbing (25) + Urgency 3 (21) + 1 student (4) = 50 pts (HIGH)
        ComplaintPriorityRecommendation fresh = prioritizer.calculatePriority(
                ComplaintCategory.PLUMBING, 3, 1, 0);

        // Aged complaint (waiting 30 hours): 50 pts + (30 * 0.5 = 15 pts) = 65 pts
        ComplaintPriorityRecommendation aged = prioritizer.calculatePriority(
                ComplaintCategory.PLUMBING, 3, 1, 30);

        assertTrue(aged.getScore() > fresh.getScore());
        assertTrue(aged.getExplanation().contains("Pending backlog duration"));
    }

    @Test
    @DisplayName("Urgency input is safely clamped between 1 and 5")
    void testUrgencyClamping() {
        ComplaintPriorityRecommendation clampedHigh = prioritizer.calculatePriority(
                ComplaintCategory.WIFI, 999, 1, 0);
        ComplaintPriorityRecommendation normalHigh = prioritizer.calculatePriority(
                ComplaintCategory.WIFI, 5, 1, 0);

        assertEquals(normalHigh.getScore(), clampedHigh.getScore());

        ComplaintPriorityRecommendation clampedLow = prioritizer.calculatePriority(
                ComplaintCategory.WIFI, -10, 1, 0);
        ComplaintPriorityRecommendation normalLow = prioritizer.calculatePriority(
                ComplaintCategory.WIFI, 1, 1, 0);

        assertEquals(normalLow.getScore(), clampedLow.getScore());
    }
}
