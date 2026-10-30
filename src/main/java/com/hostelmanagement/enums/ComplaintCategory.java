package com.hostelmanagement.enums;

/**
 * Categories of hostel complaints with base priority weights.
 * The weight influences the AI complaint prioritization score.
 */
public enum ComplaintCategory {
    WATER_SUPPLY(8),
    ELECTRICITY(9),
    PLUMBING(7),
    WIFI(5),
    CLEANING(4),
    FURNITURE(3),
    OTHER(2);

    private final int basePriorityWeight;

    ComplaintCategory(int basePriorityWeight) {
        this.basePriorityWeight = basePriorityWeight;
    }

    public int getBasePriorityWeight() {
        return basePriorityWeight;
    }
}
