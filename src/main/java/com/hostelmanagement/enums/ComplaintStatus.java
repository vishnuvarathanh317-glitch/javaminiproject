package com.hostelmanagement.enums;

/**
 * Controlled lifecycle states for a complaint.
 * Valid transitions: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
 */
public enum ComplaintStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    /**
     * Checks if transitioning from this status to the target is allowed.
     */
    public boolean canTransitionTo(ComplaintStatus target) {
        return switch (this) {
            case OPEN -> target == ASSIGNED;
            case ASSIGNED -> target == IN_PROGRESS || target == OPEN;
            case IN_PROGRESS -> target == RESOLVED;
            case RESOLVED -> target == CLOSED || target == IN_PROGRESS;
            case CLOSED -> false;
        };
    }
}
