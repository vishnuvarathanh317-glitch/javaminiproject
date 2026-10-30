package com.hostelmanagement.algorithms;

import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.model.Bed;
import com.hostelmanagement.model.WaitingListEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Waiting List Processor (Algorithm 5 & FIFO Queue)")
class WaitingListProcessorTest {

    private WaitingListEntry createEntry(int id, int studentId, String block, boolean fulfilled) {
        WaitingListEntry e = new WaitingListEntry();
        e.setId(id);
        e.setStudentRecordId(studentId);
        e.setPreferredBlockName(block);
        e.setCreatedAt(LocalDateTime.now());
        e.setFulfilled(fulfilled);
        return e;
    }

    @Test
    @DisplayName("FIFO order strictly maintained for enqueued entries")
    void testFifoOrdering() {
        WaitingListProcessor processor = new WaitingListProcessor();

        WaitingListEntry e1 = createEntry(1, 101, "A", false);
        WaitingListEntry e2 = createEntry(2, 102, "B", false);
        WaitingListEntry e3 = createEntry(3, 103, "A", false);

        processor.enqueue(e1);
        processor.enqueue(e2);
        processor.enqueue(e3);

        assertEquals(3, processor.getQueueSize());
        assertEquals(e1, processor.peekNext());
    }

    @Test
    @DisplayName("Fulfilled entries are not enqueued")
    void testFulfilledEntriesExcluded() {
        WaitingListProcessor processor = new WaitingListProcessor();

        WaitingListEntry fulfilled = createEntry(1, 101, "A", true);
        processor.enqueue(fulfilled);

        assertEquals(0, processor.getQueueSize());
        assertNull(processor.peekNext());
    }

    @Test
    @DisplayName("Poll candidate verifies eligibility and ignores already allocated students")
    void testAllocationEligibilityFiltering() {
        WaitingListProcessor processor = new WaitingListProcessor();

        // Student 101 already has a room
        WaitingListEntry e1 = createEntry(1, 101, "Block A", false);
        // Student 102 is free and wants Block A
        WaitingListEntry e2 = createEntry(2, 102, "Block A", false);

        processor.enqueue(e1);
        processor.enqueue(e2);

        Bed bed = new Bed(201, "Bed-1");
        bed.setId(1);
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setBlockName("Block A");

        List<Integer> activeStudentAllocations = List.of(101); // 101 already allocated

        WaitingListEntry allocated = processor.pollNextEligible(bed, activeStudentAllocations);

        assertNotNull(allocated);
        assertEquals(102, allocated.getStudentRecordId()); // e1 was skipped, e2 selected
    }

    @Test
    @DisplayName("Bed unavailable returns null without altering queue")
    void testBedNotAvailable() {
        WaitingListProcessor processor = new WaitingListProcessor();
        WaitingListEntry e1 = createEntry(1, 101, "Block A", false);
        processor.enqueue(e1);

        Bed occupiedBed = new Bed(201, "Bed-1");
        occupiedBed.setId(1);
        occupiedBed.setStatus(BedStatus.OCCUPIED);

        WaitingListEntry candidate = processor.pollNextEligible(occupiedBed, new ArrayList<>());

        assertNull(candidate);
        assertEquals(1, processor.getQueueSize()); // Queue remains intact
    }
}
