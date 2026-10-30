package com.hostelmanagement.ai;

import com.hostelmanagement.enums.RoomType;
import com.hostelmanagement.model.Bed;
import com.hostelmanagement.model.RoomRecommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Intelligent Room Recommendation Engine (Rule-based AI).
 *
 * Evaluates available beds against student preferences with explainable scoring.
 * Demonstrates:
 *   - DATA STRUCTURE: PriorityQueue for ranking candidate rooms
 *   - ALGORITHM: Transparent multi-criteria scoring algorithm with deterministic tie-breaking
 *   - EXPLAINABLE AI: Human-readable rationale for every recommendation
 */
public class RoomRecommendationEngine {

    public static class Preference {
        private final String preferredBlock;
        private final Integer preferredFloor;
        private final RoomType preferredRoomType;

        public Preference(String preferredBlock, Integer preferredFloor, RoomType preferredRoomType) {
            this.preferredBlock = preferredBlock;
            this.preferredFloor = preferredFloor;
            this.preferredRoomType = preferredRoomType;
        }

        public String getPreferredBlock() { return preferredBlock; }
        public Integer getPreferredFloor() { return preferredFloor; }
        public RoomType getPreferredRoomType() { return preferredRoomType; }
    }

    /**
     * Recommends beds ranked by suitability score.
     * Uses PriorityQueue data structure.
     */
    public List<RoomRecommendation> recommendBeds(List<Bed> availableBeds, Preference preference) {
        if (availableBeds == null || availableBeds.isEmpty()) {
            return new ArrayList<>();
        }

        // PriorityQueue orders by natural ordering of RoomRecommendation (highest score first)
        PriorityQueue<RoomRecommendation> queue = new PriorityQueue<>();

        for (Bed bed : availableBeds) {
            double score = 50.0; // Base score for any available bed
            List<String> reasons = new ArrayList<>();
            reasons.add("Bed is verified available");

            if (preference != null) {
                // Block preference match (+25 pts)
                if (preference.getPreferredBlock() != null && !preference.getPreferredBlock().trim().isEmpty()) {
                    if (bed.getBlockName() != null && bed.getBlockName().equalsIgnoreCase(preference.getPreferredBlock().trim())) {
                        score += 25.0;
                        reasons.add("Matches preferred block (" + bed.getBlockName() + ")");
                    }
                }

                // Floor preference match (+15 pts)
                if (preference.getPreferredFloor() != null) {
                    if (bed.getFloorNumber() == preference.getPreferredFloor()) {
                        score += 15.0;
                        reasons.add("Matches preferred floor (Floor " + bed.getFloorNumber() + ")");
                    }
                }
            }

            // Lower floor accessibility bonus (Floor 1 or Ground floor: +5 pts)
            if (bed.getFloorNumber() == 1 || bed.getFloorNumber() == 0) {
                score += 5.0;
                reasons.add("Convenient ground/lower floor access");
            }

            String explanation = String.join("; ", reasons) + ".";
            queue.offer(new RoomRecommendation(bed, score, explanation));
        }

        // Extract ordered recommendations from PriorityQueue
        List<RoomRecommendation> rankedResults = new ArrayList<>();
        while (!queue.isEmpty()) {
            rankedResults.add(queue.poll());
        }

        return rankedResults;
    }
}
