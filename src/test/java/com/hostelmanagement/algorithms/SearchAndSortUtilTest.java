package com.hostelmanagement.algorithms;

import com.hostelmanagement.enums.ComplaintPriority;
import com.hostelmanagement.enums.ComplaintStatus;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Search & Sort Utilities (Algorithm 3 & Hash Indexing)")
class SearchAndSortUtilTest {

    @Test
    @DisplayName("HashMap indexing by register number enables O(1) direct lookup")
    void testIndexStudentsByRegisterNumber() {
        Student s1 = new Student("rahul", "Rahul Sharma", "rahul@test.com", "REG2024001", "CSE", 2);
        Student s2 = new Student("priya", "Priya Nair", "priya@test.com", "REG2024002", "ECE", 1);

        Map<String, Student> index = SearchAndSortUtil.indexStudentsByRegisterNumber(List.of(s1, s2));

        assertEquals(2, index.size());
        assertEquals("Rahul Sharma", index.get("REG2024001").getFullName());
        assertEquals("Priya Nair", index.get("REG2024002").getFullName());
        assertNull(index.get("NONEXISTENT"));
    }

    @Test
    @DisplayName("Multi-field student search finds matches in name, regNo, and department")
    void testSearchStudents() {
        Student s1 = new Student("rahul", "Rahul Sharma", "rahul@test.com", "REG101", "CSE", 2);
        Student s2 = new Student("anita", "Anita Roy", "anita@test.com", "REG102", "Mechanical", 3);

        List<Student> list = List.of(s1, s2);

        // Search by partial name
        List<Student> res1 = SearchAndSortUtil.searchStudents(list, "sharma");
        assertEquals(1, res1.size());
        assertEquals("Rahul Sharma", res1.get(0).getFullName());

        // Search by register number
        List<Student> res2 = SearchAndSortUtil.searchStudents(list, "REG102");
        assertEquals(1, res2.size());
        assertEquals("Anita Roy", res2.get(0).getFullName());

        // Search by department
        List<Student> res3 = SearchAndSortUtil.searchStudents(list, "mechanical");
        assertEquals(1, res3.size());
        assertEquals("Anita Roy", res3.get(0).getFullName());
    }

    @Test
    @DisplayName("Complaints are sorted with CRITICAL first, then HIGH, MEDIUM, LOW")
    void testSortComplaintsByPriorityAndDate() {
        List<Complaint> complaints = new ArrayList<>();

        Complaint cLow = new Complaint();
        cLow.setId(1);
        cLow.setPriority(ComplaintPriority.LOW);
        cLow.setStatus(ComplaintStatus.OPEN);
        cLow.setDescription("Desc 1");
        cLow.setCreatedAt(LocalDateTime.now());

        Complaint cCritical = new Complaint();
        cCritical.setId(2);
        cCritical.setPriority(ComplaintPriority.CRITICAL);
        cCritical.setStatus(ComplaintStatus.OPEN);
        cCritical.setDescription("Desc 2");
        cCritical.setCreatedAt(LocalDateTime.now());

        Complaint cMedium = new Complaint();
        cMedium.setId(3);
        cMedium.setPriority(ComplaintPriority.MEDIUM);
        cMedium.setStatus(ComplaintStatus.OPEN);
        cMedium.setDescription("Desc 3");
        cMedium.setCreatedAt(LocalDateTime.now());

        complaints.add(cLow);
        complaints.add(cCritical);
        complaints.add(cMedium);

        SearchAndSortUtil.sortComplaintsByPriorityAndDate(complaints);

        assertEquals(ComplaintPriority.CRITICAL, complaints.get(0).getPriority());
        assertEquals(ComplaintPriority.MEDIUM, complaints.get(1).getPriority());
        assertEquals(ComplaintPriority.LOW, complaints.get(2).getPriority());
    }
}
