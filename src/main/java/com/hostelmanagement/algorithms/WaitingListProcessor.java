package com.hostelmanagement.algorithms;

import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.model.Bed;
import com.hostelmanagement.model.WaitingListEntry;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * FIFO Waiting-List Queue Processor (Algorithm 5 & Data Structure 3).
 *
 * Demonstrates:
 *   - DATA STRUCTURE: Queue (FIFO ArrayDeque) for fair order processing
 *   - ALGORITHM: Rigorous re-validation of eligibility and bed availability
 *     before confirming candidates (prevents naive front-of-queue allocation).
 */
public class WaitingListProcessor {

    private final Queue<WaitingListEntry> fifoQueue;

    public WaitingListProcessor() {
        this.fifoQueue = new ArrayDeque<>();
    }

    public WaitingListProcessor(List<WaitingListEntry> persistedEntries) {
        this.fifoQueue = new ArrayDeque<>();
        if (persistedEntries != null) {
            for (WaitingListEntry entry : persistedEntries) {
                if (!entry.isFulfilled()) {
                    fifoQueue.offer(entry);
                }
            }
        }
    }

    public void enqueue(WaitingListEntry entry) {
        if (entry != null && !entry.isFulfilled()) {
            fifoQueue.offer(entry);
        }
    }

    public WaitingListEntry peekNext() {
        return fifoQueue.peek();
    }

    public int getQueueSize() {
        return fifoQueue.size();
    }

    /**
     * Finds the next eligible candidate for a given available bed.
     * Re-verifies bed status and student eligibility before dequeuing.
     */
    public WaitingListEntry pollNextEligible(Bed bed, List<Integer> currentlyAllocatedStudentIds) {
        if (bed == null || bed.getStatus() != BedStatus.AVAILABLE) {
            return null; // Bed is not available
        }

        int size = fifoQueue.size();
        for (int i = 0; i < size; i++) {
            WaitingListEntry candidate = fifoQueue.poll();
            if (candidate == null) break;

            // Integrity check 1: Student must not already have an active allocation
            if (currentlyAllocatedStudentIds != null && currentlyAllocatedStudentIds.contains(candidate.getStudentRecordId())) {
                // Student was already allocated elsewhere; mark skipped, do not re-enqueue
                continue;
            }

            // Integrity check 2: Preference matching if specified
            boolean blockMatches = candidate.getPreferredBlockName() == null
                    || candidate.getPreferredBlockName().trim().isEmpty()
                    || candidate.getPreferredBlockName().equalsIgnoreCase(bed.getBlockName());

            if (blockMatches) {
                // Found next eligible candidate!
                return candidate;
            } else {
                // Candidate preferences don't match this bed; keep in queue
                fifoQueue.offer(candidate);
            }
        }

        return null;
    }
}
