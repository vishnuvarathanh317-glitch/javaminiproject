package com.hostelmanagement.model;

/**
 * AI-generated room recommendation with explainable scoring.
 * Used by the Smart Room Recommendation Engine.
 *
 * Demonstrates DATA STRUCTURES: these objects are ranked in a PriorityQueue.
 */
public class RoomRecommendation implements Comparable<RoomRecommendation> {
    private final Bed bed;
    private final double score;
    private final String explanation;

    public RoomRecommendation(Bed bed, double score, String explanation) {
        this.bed = bed;
        this.score = score;
        this.explanation = explanation;
    }

    public Bed getBed() { return bed; }
    public double getScore() { return score; }
    public String getExplanation() { return explanation; }

    /** Higher score = better recommendation (natural ordering descending). */
    @Override
    public int compareTo(RoomRecommendation other) {
        int cmp = Double.compare(other.score, this.score); // descending
        if (cmp == 0) {
            // Deterministic tie-breaking: lower bed ID first
            cmp = Integer.compare(this.bed.getId(), other.bed.getId());
        }
        return cmp;
    }

    @Override
    public String toString() {
        return String.format("Bed %s (Room %s) — Score: %.1f — %s",
                bed.getBedNumber(), bed.getRoomNumber(), score, explanation);
    }
}
