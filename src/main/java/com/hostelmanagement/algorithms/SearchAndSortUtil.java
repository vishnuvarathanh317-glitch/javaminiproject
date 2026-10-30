package com.hostelmanagement.algorithms;

import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.Room;
import com.hostelmanagement.model.Student;

import java.util.*;

/**
 * Search and Sorting Utilities (Algorithm 3 & Data Structure 1).
 *
 * Demonstrates:
 *   - DATA STRUCTURE: Hash-based indexing (HashMap) for O(1) fast lookup
 *   - ALGORITHM: Multi-attribute search and custom Comparator-based sorting
 */
public final class SearchAndSortUtil {

    private SearchAndSortUtil() {}

    // ── DATA STRUCTURE 1: HASH-BASED INDEXING ───────────────────

    /**
     * Builds a hash index of Students by their Register Number for O(1) lookups.
     */
    public static Map<String, Student> indexStudentsByRegisterNumber(List<Student> students) {
        Map<String, Student> index = new HashMap<>();
        if (students != null) {
            for (Student s : students) {
                if (s.getRegisterNumber() != null) {
                    index.put(s.getRegisterNumber().toUpperCase(), s);
                }
            }
        }
        return index;
    }

    /**
     * Groups rooms by Floor ID using a HashMap for multi-level lookup.
     */
    public static Map<Integer, List<Room>> groupRoomsByFloor(List<Room> rooms) {
        Map<Integer, List<Room>> grouped = new HashMap<>();
        if (rooms != null) {
            for (Room r : rooms) {
                grouped.computeIfAbsent(r.getFloorId(), k -> new ArrayList<>()).add(r);
            }
        }
        return grouped;
    }

    // ── ALGORITHM 3: SEARCHING & COMPARATOR SORTING ─────────────

    /**
     * Filters students matching a query against name, register number, or department.
     */
    public static List<Student> searchStudents(List<Student> students, String query) {
        if (students == null) return new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(students);
        }

        String q = query.trim().toLowerCase();
        List<Student> results = new ArrayList<>();
        for (Student s : students) {
            boolean matchesName = s.getFullName() != null && s.getFullName().toLowerCase().contains(q);
            boolean matchesReg = s.getRegisterNumber() != null && s.getRegisterNumber().toLowerCase().contains(q);
            boolean matchesDept = s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q);

            if (matchesName || matchesReg || matchesDept) {
                results.add(s);
            }
        }
        return results;
    }

    /**
     * Sorts complaints by severity (CRITICAL > HIGH > MEDIUM > LOW), then by date (oldest first).
     */
    public static void sortComplaintsByPriorityAndDate(List<Complaint> complaints) {
        if (complaints == null || complaints.size() <= 1) return;

        complaints.sort(Comparator
                .comparing(Complaint::getPriority, (p1, p2) -> {
                    // Critical first
                    int r1 = p1 == null ? 0 : p1.ordinal();
                    int r2 = p2 == null ? 0 : p2.ordinal();
                    return Integer.compare(r2, r1); // descending ordinal
                })
                .thenComparing(Complaint::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * Sorts rooms by block name, floor number, then room number.
     */
    public static void sortRooms(List<Room> rooms) {
        if (rooms == null || rooms.size() <= 1) return;

        rooms.sort(Comparator
                .comparing(Room::getBlockName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparingInt(Room::getFloorNumber)
                .thenComparing(Room::getRoomNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
    }
}
