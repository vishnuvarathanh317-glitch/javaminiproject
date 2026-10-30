package com.hostelmanagement.model;

import com.hostelmanagement.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Models & OOP Principles (Inheritance, Polymorphism, Encapsulation)")
class DomainModelTest {

    @Test
    @DisplayName("Polymorphic role description dynamically dispatches according to subclass")
    void testPolymorphism() {
        User admin = new Administrator("admin", "Admin User", "admin@hostel.com");
        Warden warden = new Warden("warden", "Warden Kumar", "warden@hostel.com");
        warden.setAssignedBlockName("Main Block");
        User student = new Student("student", "Rahul S", "rahul@hostel.com", "REG101", "CSE", 3);

        assertEquals(UserRole.ADMIN, admin.getRole());
        assertEquals(UserRole.WARDEN, warden.getRole());
        assertEquals(UserRole.STUDENT, student.getRole());

        // Polymorphic call: getRoleDescription()
        assertTrue(admin.getRoleDescription().contains("System Administrator"));
        assertTrue(warden.getRoleDescription().contains("Hostel Warden"));
        assertTrue(student.getRoleDescription().contains("Student — CSE, Year 3"));
    }

    @Test
    @DisplayName("Encapsulation prevents direct state alteration and enforces valid defaults")
    void testEncapsulation() {
        Student s = new Student();
        s.setFullName("John Doe");
        s.setRegisterNumber("REG2024");
        s.setDepartment("IT");
        s.setYear(2);

        assertEquals("John Doe", s.getFullName());
        assertEquals("REG2024", s.getRegisterNumber());
        assertEquals("IT", s.getDepartment());
        assertEquals(2, s.getYear());
        assertEquals(UserRole.STUDENT, s.getRole());
    }

    @Test
    @DisplayName("Active status defaults to true upon creation")
    void testUserActiveStatus() {
        Administrator a = new Administrator("sysadmin", "Root Admin", "admin@hostel.com");
        assertTrue(a.isActive());

        a.setActive(false);
        assertFalse(a.isActive());
    }
}
