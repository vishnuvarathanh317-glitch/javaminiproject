# Smart Hostel Management System

**Course:** CSE203 — Object-Oriented Programming Using Java (Java Mini Project)  
**Architecture:** JavaFX (Desktop UI) + Core Java (OOP & Algorithms) + MySQL (JDBC Persistence)  
**Technology Stack:** Java 21, JavaFX 21.0.2, MySQL Connector/J 8.3.0, JUnit 5.10.2, Maven  

---

## 📌 Executive Overview

The **Smart Hostel Management System** is a production-grade, enterprise-ready desktop application built in modern Java to automate and modernize college hostel administration. Designed for three distinct roles (**System Administrator**, **Hostel Warden**, and **Student Resident**), the software manages the complete operational lifecycle—from room allocations and leave approvals to fee tracking, automated attendance, and rule-based AI grievance triage.

Unlike basic CRUD demo projects, this system integrates:
- **Core OOP Principles:** Class inheritance (`User` base with `Administrator`, `Warden`, and `Student` subclasses), runtime polymorphism, strict encapsulation, and composite entity relationships.
- **Advanced Data Structures:** Hash-based indexing (`HashMap`) for $O(1)$ student record retrieval, fair order queue processing (`ArrayDeque` FIFO queue), and optimal priority ranking (`PriorityQueue`).
- **Rule-Based AI Algorithms:** Explainable multi-factor room recommendation engine and dynamic complaint priority scoring with backlog aging escalation.
- **Concurrency & Transaction Safety:** Pessimistic locking (`SELECT FOR UPDATE`) prevents double-booking of beds during concurrent allocations.
- **Enterprise Security:** Salted `PBKDF2WithHmacSHA256` password hashing with constant-time byte comparison preventing timing attacks.

---

## 🚀 Key Features by User Role

### 🛡️ System Administrator
- **Hostel Hierarchy Builder:** Configure Hostels, Blocks, Floors, Rooms, and individual Beds.
- **User Account Management:** Create and toggle credentials for Wardens and Students with role validation.
- **Institutional Reports & CSV Export:** Live aggregated analytics on occupancy, fee collections, and maintenance backlog, exportable with one click to UTF-8 CSV.
- **One-Click Database Seeding:** Instant population of realistic test data (hostels, rooms, students, fees, complaints).

### 📋 Hostel Warden
- **Automated & Manual Room Allocation:** Review bed availability, view AI room suitability scores, and allocate rooms.
- **FIFO Waiting-List Management:** Transparent queue allocation ensuring fairness when rooms are fully booked.
- **Complaint Triage & Lifecycle:** Track complaints through `OPEN` → `ASSIGNED` → `IN_PROGRESS` → `RESOLVED` → `CLOSED` states.
- **Leave Request Approvals:** Approve or reject student outing/vacation requests with date validation.
- **Daily Attendance Logging:** Bulk mark attendance (`PRESENT`, `ABSENT`, `ON_LEAVE`, `LATE`) per block.
- **Notice Board Broadcasts:** Post and manage campus-wide circulars with importance badges.

### 🎓 Student Resident
- **Personalized Resident Dashboard:** View allocated room, roommate information, fee balance, and pending leaves.
- **AI-Guided Room Selection:** Request preferred blocks and floors; review transparent suitability score breakdowns.
- **Maintenance Grievance Portal:** Log complaints with real-time AI priority preview based on urgency, category, and student impact.
- **Digital Leave Application:** Submit leave dates with automated validation against historical records.
- **Fee Ledger & Payment Receipts:** View installment invoices, record payments, and download payment summaries.

---

## ⚙️ Prerequisites & Environment Setup

1. **Java Development Kit (JDK):** Version 21 or newer ([Eclipse Temurin / Oracle JDK](https://adoptium.net/)). Verify with:
   ```powershell
   java -version
   javac -version
   ```
2. **Apache Maven:** Version 3.8+ installed and available on `PATH`. Verify with:
   ```powershell
   mvn -version
   ```
3. **MySQL Server:** Version 8.0 or newer running locally or on a LAN network. Verify with:
   ```powershell
   Get-Service -Name '*mysql*'
   ```

---

## 🛠️ Step-by-Step Installation & Running

### Step 1: Clone or Open Workspace
Open a PowerShell terminal in the project directory:
```powershell
cd c:\Users\vishnuvarathanh\OneDrive\Desktop\javaminiprojectcse203
```

### Step 2: Configure Database Credentials
A configuration template is provided at `src/main/resources/application.properties.example`.
Edit `src/main/resources/application.properties` with your local MySQL password:
```properties
db.url=jdbc:mysql://localhost:3306/smart_hostel_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YourMySQLPasswordHere
```
> **Automatic Schema Migration:** You do **not** need to manually run SQL scripts or create tables! Upon startup, `DatabaseInitializer` automatically creates `smart_hostel_db` and applies `V1__initial_schema.sql` idempotently.

### Step 3: Run the Automated Unit Test Suite
Verify that all 32 unit tests pass:
```powershell
mvn test
```

### Step 4: Launch the Application
You can run the application directly via the JavaFX Maven plugin:
```powershell
mvn javafx:run
```
Or build and execute the standalone shaded JAR:
```powershell
mvn package
java -jar target/smart-hostel-management-1.0-SNAPSHOT.jar
```

---

## 🔑 Default Credentials & First-Run Setup

### 🌟 First-Run Setup Wizard
If the database contains no administrator account, the application automatically presents the **First-Run Setup Wizard** prompting you to create your initial master admin account.

### 🧪 Sample Data Seeding
Once logged in as an Administrator, click **"Seed Demo Data"** on the dashboard. This pre-populates:
- **Hostel:** Everest Boys Hostel & Sarojini Girls Hostel
- **Blocks & Floors:** Block A (Ground & 1st Floor) with Single and Double Rooms
- **Users:**
  - **Admin:** `admin` / `admin123`
  - **Warden:** `warden` / `warden123`
  - **Student 1:** `rahul` / `student123` (Reg: `REG2024001`)
  - **Student 2:** `priya` / `student123` (Reg: `REG2024002`)
- **Sample Records:** Allocations, leave requests, maintenance complaints with AI priorities, fee schedules, and notice broadcasts.

---

## 📂 Project Structure

```
javaminiprojectcse203/
├── pom.xml                               # Maven project configuration (Java 21, JavaFX, MySQL, JUnit)
├── README.md                             # Project setup & quickstart guide
├── docs/                                 # Academic documentation & viva preparation
│   ├── ARCHITECTURE.md                   # System design, OOP mappings & layered structure
│   ├── DATABASE.md                       # Relational schema dictionary & ER diagram
│   ├── USER_GUIDE.md                     # Step-by-step screenshots & workflow walkthrough
│   ├── VIVA_QUESTIONS.md                 # 25+ viva interview questions with answers
│   └── PROJECT_REPORT.md                 # College project report draft
└── src/
    ├── main/
    │   ├── java/com/hostelmanagement/
    │   │   ├── Launcher.java             # Non-modular bootstrap entry point
    │   │   ├── MainApp.java              # JavaFX Application lifecycle & startup
    │   │   ├── ai/                       # Explainable AI engines
    │   │   │   ├── RoomRecommendationEngine.java
    │   │   │   └── ComplaintPrioritizer.java
    │   │   ├── algorithms/               # Algorithms & Data Structures
    │   │   │   ├── OccupancyCalculator.java
    │   │   │   ├── SearchAndSortUtil.java
    │   │   │   └── WaitingListProcessor.java
    │   │   ├── config/                   # Configuration management
    │   │   ├── database/                 # Connection pooling & schema migrations
    │   │   ├── enums/                    # 10 Strongly-typed domain enums
    │   │   ├── exception/                # Custom business exception hierarchy
    │   │   ├── model/                    # 19 Domain entity classes
    │   │   ├── repository/               # JDBC Data Access Objects (DAOs)
    │   │   ├── security/                 # PBKDF2 Password hashing & SessionManager
    │   │   ├── service/                  # Business logic & transactional services
    │   │   ├── ui/                       # 15 JavaFX UI dashboard & management screens
    │   │   └── util/                     # Validation, CSV export, and date utilities
    │   └── resources/
    │       ├── application.properties    # Active database configuration
    │       ├── css/style.css             # Modern Slate/Navy dashboard styling
    │       └── db/migration/
    │           └── V1__initial_schema.sql # 20 relational database tables
    └── test/
        └── java/com/hostelmanagement/    # 32 JUnit 5 test cases across 8 test suites
```

---

## 🧪 Verified Automated Test Suite

| Test Suite | Focus Area | Status |
| :--- | :--- | :---: |
| `RoomRecommendationEngineTest` | PriorityQueue ordering, multi-factor scoring, explainable rationale | **PASS** |
| `ComplaintPrioritizerTest` | AI priority threshold classification, urgency clamping, backlog aging | **PASS** |
| `OccupancyCalculatorTest` | Division-by-zero protection, 0% beds, 100% capacity, maintenance filters | **PASS** |
| `WaitingListProcessorTest` | FIFO ArrayDeque queueing, already-allocated eligibility skip | **PASS** |
| `SearchAndSortUtilTest` | $O(1)$ HashMap student index lookup, Comparator priority sorting | **PASS** |
| `DomainModelTest` | OOP inheritance, dynamic polymorphic dispatch, encapsulation | **PASS** |
| `PasswordUtilTest` | PBKDF2WithHmacSHA256 salted hashing, timing-safe equality | **PASS** |
| `DateUtilTest` | Standardized `dd-MM-yyyy` formatting and null safety | **PASS** |
| `InputValidatorTest` | Regex email validation, 10-12 digit phone validation, leave date logic | **PASS** |

**Execution Result:** `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0` (100% Passing).

---

## 📜 Documentation Links

- Detailed Architecture & Design Patterns: [docs/ARCHITECTURE.md](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/javaminiprojectcse203/docs/ARCHITECTURE.md)
- Relational Database Dictionary & ER Diagram: [docs/DATABASE.md](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/javaminiprojectcse203/docs/DATABASE.md)
- End-User Role Navigation Guide: [docs/USER_GUIDE.md](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/javaminiprojectcse203/docs/USER_GUIDE.md)
- Viva Voce Questions & Answers: [docs/VIVA_QUESTIONS.md](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/javaminiprojectcse203/docs/VIVA_QUESTIONS.md)
- Full College Project Report: [docs/PROJECT_REPORT.md](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/javaminiprojectcse203/docs/PROJECT_REPORT.md)
