# Academic Mini-Project Report

## **SMART HOSTEL MANAGEMENT SYSTEM**
### An Object-Oriented Desktop Application for Institutional Residence Management

**Course:** CSE203 — Object-Oriented Programming Using Java  
**Program:** Bachelor of Technology (B.Tech) in Computer Science & Engineering  
**Academic Year:** 2026–2027  

---

## Abstract

Hostel accommodation and residential facility administration in modern higher education institutions face recurring operational challenges: manual room allotment prone to room allocation conflicts, lack of transparent waiting list prioritization, delayed grievance resolution, and uncoordinated fee records. 

The **Smart Hostel Management System** addresses these issues through a desktop application built using **Java 21**, **JavaFX**, and a **MySQL** relational database backend. The system demonstrates core Object-Oriented Programming (OOP) paradigms (inheritance, dynamic polymorphism, encapsulation, and composition) integrated with fundamental data structures and algorithmic designs:
1. Hash-based indexing (`HashMap`) for $O(1)$ student record search.
2. A binary heap priority queue (`PriorityQueue`) powering a rule-based explainable AI room recommendation engine.
3. A first-in, first-out (`ArrayDeque`) queue processor for fair waiting-list fulfillment.
4. Dynamic complaint prioritization with automated backlog aging escalation.
5. Pessimistic concurrency locking (`SELECT FOR UPDATE`) to prevent bed double-booking during concurrent allocations.

Comprehensive automated testing utilizing JUnit 5 validates all algorithmic constraints and edge cases with 100% test success across 32 unit test cases.

---

## Chapter 1: Introduction

### 1.1 Background & Motivation
College and university hostels accommodate thousands of students across multiple buildings, blocks, and room categories. Traditionally, institutions rely on paper registries or spreadsheets, resulting in data redundancy, double-booked beds, lack of transparent grievance tracking, and delayed emergency response.

### 1.2 Problem Statement
Existing manual and basic software systems suffer from:
- **Allocation Inefficiencies:** Subjective or biased room allocation without preference optimization.
- **Race Conditions:** Lack of database concurrency protection during admission cycles leading to duplicate room assignments.
- **Maintenance Bottlenecks:** FIFO grievance handling that treats non-urgent furniture requests with the same priority as emergency water or electrical outages.
- **Security Vulnerabilities:** Storage of plain-text passwords and susceptibility to SQL injection.

### 1.3 Project Objectives
The primary objectives of this project are:
1. To model hostel operations using clean, modular Object-Oriented Design in Java.
2. To provide role-tailored user interfaces for Administrators, Wardens, and Students.
3. To implement explainable algorithmic decision-making for room recommendation and grievance prioritization.
4. To implement transaction-safe concurrency controls and enterprise-grade cryptographic security.
5. To provide automated database migrations and comprehensive unit test coverage.

---

## Chapter 2: System Analysis & Requirements

### 2.1 Functional Requirements
- **FR1 (Authentication & Access Control):** Role-based login supporting Administrator, Warden, and Student roles with salted password hashing.
- **FR2 (Hostel Infrastructure Hierarchy):** Hierarchical modeling of Hostels, Blocks, Floors, Rooms, and Beds with dynamic occupancy tracking.
- **FR3 (Smart Allocation Engine):** Algorithmic matching of student room preferences with available beds using priority ranking.
- **FR4 (Waiting List Management):** Fair FIFO queue management for waitlisted students when capacity is reached.
- **FR5 (Grievance Lifecycle & AI Triage):** Multi-factor priority scoring (category severity, student urgency, affected population, waiting time) and 5-state lifecycle management (`OPEN` → `CLOSED`).
- **FR6 (Leave Application Workflow):** Student leave submission with date validation and warden approval tracking.
- **FR7 (Daily Attendance Registry):** Daily attendance logging with batch commit support.
- **FR8 (Financial Ledger):** Term fee billing, payment recording, and balance tracking.
- **FR9 (Institutional Reporting):** Real-time aggregation of occupancy, grievances, and finances with CSV export.

### 2.2 Non-Functional Requirements
- **Data Integrity & Consistency:** ACID compliance enforced through MySQL InnoDB foreign keys and transactional rollbacks.
- **Performance:** Sub-second response times for search and algorithmic calculations ($O(1)$ hash indexing).
- **Security:** Salted PBKDF2WithHmacSHA256 password hashing and constant-time equality comparisons to prevent timing attacks.
- **Portability:** Desktop application running on Windows, macOS, and Linux with Java 21 runtime.

---

## Chapter 3: System Design & Architecture

### 3.1 Layered Architecture
The project adheres to a four-tier architecture:
- **Presentation Layer (`com.hostelmanagement.ui`):** JavaFX screens styled with modern CSS.
- **Service Layer (`com.hostelmanagement.service`):** Business logic and validation.
- **Algorithmic & AI Engine Layer (`com.hostelmanagement.ai`, `algorithms`):** Optimization and decision logic.
- **Data Access Layer (`com.hostelmanagement.repository`, `database`):** JDBC DAO repositories and connection pooling.

### 3.2 OOP Domain Model & Inheritance Hierarchy
```
           +-----------------------+
           |     abstract User     |
           +-----------------------+
           | - id: int             |
           | - username: String    |
           | - passwordHash: String|
           | - fullName: String    |
           | - email: String       |
           | - role: UserRole      |
           +-----------------------+
           | + getRoleDescription()|
           +-----------------------+
                      ^
         +------------+------------+
         |                         |                         |
+-------------------+     +-----------------+     +---------------------+
|   Administrator   |     |     Warden      |     |       Student       |
+-------------------+     +-----------------+     +---------------------+
|                   |     | - assignedBlock |     | - studentRecordId   |
|                   |     |                 |     | - registerNumber    |
|                   |     |                 |     | - department: String|
|                   |     |                 |     | - year: int         |
+-------------------+     +-----------------+     +---------------------+
```

---

## Chapter 4: Algorithmic & Implementation Details

### 4.1 Room Recommendation Engine (`PriorityQueue`)
Candidate rooms are evaluated against student preferences (Block, Floor, Room Type):
$$\text{Suitability Score} = 50.0 + \Delta_{\text{Block}}(25.0) + \Delta_{\text{Floor}}(15.0) + \Delta_{\text{GroundBonus}}(5.0)$$
Candidate beds are stored in a binary max-heap `PriorityQueue<RoomRecommendation>`. The highest-scoring rooms are extracted in $O(\log n)$ time with explainable rationale strings.

### 4.2 Dynamic Complaint Prioritization Engine
Grievance tickets are classified into `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` via a multi-factor formula:
$$\text{Score} = S_{\text{category}} + (7 \times \text{Urgency}) + \min(20, 4 \times \text{Impact}) + \min(15, 0.5 \times \text{Hours})$$
This prevents low-urgency maintenance requests from starvation by automatically elevating their score as waiting hours accumulate.

### 4.3 Concurrency Control (`SELECT FOR UPDATE`)
When allocating a bed, the transaction executes:
```sql
SELECT status FROM beds WHERE id = ? FOR UPDATE;
```
This row-level exclusive lock guarantees that concurrent allocation attempts on the same bed are serialized, eliminating race conditions.

---

## Chapter 5: Testing & Verification

Automated testing was conducted using **JUnit 5**.

### 5.1 Test Execution Summary

| Test Suite | Class Tested | Test Count | Result |
| :--- | :--- | :---: | :---: |
| `ComplaintPrioritizerTest` | `ComplaintPrioritizer` | 4 | Passed |
| `RoomRecommendationEngineTest` | `RoomRecommendationEngine` | 3 | Passed |
| `OccupancyCalculatorTest` | `OccupancyCalculator` | 4 | Passed |
| `SearchAndSortUtilTest` | `SearchAndSortUtil` | 3 | Passed |
| `WaitingListProcessorTest` | `WaitingListProcessor` | 4 | Passed |
| `DomainModelTest` | `User`, `Student`, `Warden` | 3 | Passed |
| `PasswordUtilTest` | `PasswordUtil` | 4 | Passed |
| `DateUtilTest` | `DateUtil` | 2 | Passed |
| `InputValidatorTest` | `InputValidator` | 5 | Passed |
| **Total** | | **32** | **100% Passed** |

### 5.2 Edge Cases Validated
- **Division-by-Zero Defense:** Verified that 0 total beds or 100% maintenance beds yield an occupancy rate of 0.0% without runtime arithmetic exceptions.
- **Timing Attack Resistance:** Verified that constant-time password hash comparison correctly validates matching passwords and rejects mismatches across all byte positions.
- **FIFO Waiting List Integrity:** Verified that students who obtain allocations outside the waiting queue are automatically skipped when the queue advances.

---

## Chapter 6: Conclusion & Future Scope

The **Smart Hostel Management System** fulfills the academic requirements of CSE203 by integrating Core Java Object-Oriented Programming, Data Structures, rule-based AI algorithms, and ACID-compliant relational persistence. 

### Future Enhancements
1. **Biometric Hardware Integration:** Direct serial/USB connection to biometric fingerprint and RFID scanners for turnstile attendance.
2. **Payment Gateway Integration:** Direct REST API webhook integration with institutional payment processors.
3. **Push Notifications:** Automated SMS/email alerts to parents upon student leave approval or missed curfew attendance.
