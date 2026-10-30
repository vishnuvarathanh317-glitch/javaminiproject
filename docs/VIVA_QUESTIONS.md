# College Viva Voce Examination Guide
## CSE203: Object-Oriented Programming Using Java
### Project: Smart Hostel Management System

---

## 📚 Section 1: Core Java & Object-Oriented Programming (OOP)

### Q1: How does your project demonstrate class inheritance?
**Answer:**  
We defined an abstract base class `com.hostelmanagement.model.User` that encapsulates common credentials and demographic fields (`id`, `username`, `passwordHash`, `fullName`, `email`, `role`, `active`). Three distinct child classes extend `User`:
- `Administrator`: Adds administrative rights and management flags.
- `Warden`: Extends `User` with `assignedBlockName` and operational authority.
- `Student`: Extends `User` with academic attributes (`registerNumber`, `department`, `year`, `guardianName`, `guardianPhone`).  
This eliminates duplicate code and organizes data models in a natural hierarchical structure.

### Q2: What is runtime polymorphism, and where is it used in your code?
**Answer:**  
Runtime polymorphism (dynamic method dispatch) allows a method call to resolve to different implementations at runtime based on the actual object instance. In our application, the abstract base class `User` declares:
```java
public abstract String getRoleDescription();
```
Each subclass provides its own specialized implementation:
- `Administrator` returns `"System Administrator — full access to manage the hostel system"`
- `Warden` returns `"Hostel Warden — manages students, allocations, complaints, and attendance"`
- `Student` returns `"Student — " + department + ", Year " + year`  
In `MainLayout` and `SessionManager`, our code calls `currentUser.getRoleDescription()` without knowing or caring which subclass is active, and Java invokes the appropriate override dynamically.

### Q3: How is encapsulation enforced across the project?
**Answer:**  
Encapsulation is achieved by:
1. Declaring all instance fields as `private` or `protected`.
2. Restricting access through public getter and setter methods.
3. Performing parameter validation inside setters and utility classes (`InputValidator`). For instance, `InputValidator.validateEmail()` checks against RFC-compliant regex patterns, and `InputValidator.validateLeaveDates()` guarantees that leave end dates cannot precede start dates, protecting the internal consistency of objects.

### Q4: What is the difference between Composition and Aggregation in your design?
**Answer:**  
- **Composition (Part-Of, Strong Lifecycle Dependency):** The physical building hierarchy is modeled as strict composition. A `Hostel` owns `Block`s; a `Block` owns `Floor`s; a `Floor` owns `Room`s; a `Room` owns `Bed`s. If a room is destroyed, its beds cease to exist. In MySQL, this is enforced via `ON DELETE CASCADE`.
- **Aggregation (Has-A, Weak Lifecycle Dependency):** An `Allocation` links a `Student` with a `Bed`. When a student vacates their bed, the `Allocation` status changes to `VACATED`, but neither the student's user account nor the physical bed is deleted.

### Q5: Why did you use an abstract class for `User` instead of an interface?
**Answer:**  
An abstract class was chosen because `Administrator`, `Warden`, and `Student` share actual common state (identity attributes like `id`, `username`, `email`, `passwordHash`) and common method logic (`isActive()`, `getCreatedAt()`). Interfaces in Java define contracts and static constants; they cannot hold mutable instance state or shared constructors.

---

## 🗄️ Section 2: Data Structures & Algorithmic Design

### Q6: What data structures did you implement, and what are their time complexities?
**Answer:**  
1. **Hash-Based Indexing (`HashMap<String, Student>`):** Used in `SearchAndSortUtil` to index students by register number. Time complexity: $O(1)$ average time for search and retrieval versus $O(n)$ linear list iteration.
2. **Priority Heap (`PriorityQueue<RoomRecommendation>`):** Used in `RoomRecommendationEngine` to rank candidate rooms according to multi-factor suitability scores. Time complexity: $O(\log n)$ insertion and $O(1)$ peek at the top candidate.
3. **FIFO Queue (`ArrayDeque<WaitingListEntry>`):** Used in `WaitingListProcessor` to maintain student waiting lists in fair first-in, first-out order. Time complexity: $O(1)$ for both `enqueue` (offer) and `dequeue` (poll).

### Q7: How does your rule-based AI Room Recommendation Engine work?
**Answer:**  
The `RoomRecommendationEngine` evaluates candidate beds against student preferences using a transparent mathematical scoring function:
$$\text{Score} = \text{Base}(50) + \text{BlockMatch}(25) + \text{FloorMatch}(15) + \text{LowerFloorBonus}(5)$$
Beds are placed into a `PriorityQueue` ordered by `RoomRecommendation.compareTo()`, which sorts in descending score order. Along with the score, it outputs human-readable explanations (e.g., *"Matches preferred block; Convenient ground floor access"*), providing explainable AI.

### Q8: How does the Complaint Prioritization algorithm prevent maintenance starvation?
**Answer:**  
In `ComplaintPrioritizer`, the score incorporates four factors:
1. Category baseline severity (Water/Electricity = +35 pts, Plumbing = +25 pts, WiFi = +15 pts).
2. Student-reported urgency scale (1–5 points $\times$ 7 = up to 35 pts).
3. Impact count (number of residents affected $\times$ 4 = up to 20 pts).
4. **Aging Escalation Factor:** $0.5$ points are added for every elapsed hour since submission (up to a 15-point ceiling).  
This prevents lower-severity tickets from being starved indefinitely: as an unresolved complaint sits in the queue, its aging points gradually escalate its priority from `LOW` to `MEDIUM` to `HIGH`.

### Q9: How do you protect against division-by-zero errors in occupancy calculations?
**Answer:**  
In `OccupancyCalculator`, the occupancy rate formula is:
$$\text{Rate} = \left(\frac{\text{Occupied Beds}}{\text{Usable Beds}}\right) \times 100$$
Where $\text{Usable Beds} = \text{Occupied} + \text{Available}$ (excluding beds under maintenance). If a new block has zero usable beds, standard division would trigger an `ArithmeticException` or return `NaN`. We guard this condition:
```java
double rate = usable > 0 ? ((double) occupied / usable) * 100.0 : 0.0;
```
Unit tests in `OccupancyCalculatorTest` explicitly verify this edge case.

---

## 🔒 Section 3: Concurrency, JDBC & Database Persistence

### Q10: How do you prevent double-booking when multiple wardens allocate rooms simultaneously?
**Answer:**  
We use **Pessimistic Locking** inside a database transaction:
1. In `AllocationRepository.allocateBed()`, we disable auto-commit: `conn.setAutoCommit(false)`.
2. We query the bed with an exclusive lock:
   ```sql
   SELECT status FROM beds WHERE id = ? FOR UPDATE;
   ```
3. If another transaction holds the lock, MySQL blocks the second caller until the first commits or rolls back.
4. If the bed is verified as `OCCUPIED`, our code throws `AllocationConflictException`, triggers `conn.rollback()`, and informs the user.
5. If available, the status is updated, the allocation record is inserted, and `conn.commit()` atomizes the change.

### Q11: What is the difference between `Statement` and `PreparedStatement`?
**Answer:**  
- `Statement`: Compiles SQL dynamically every time it is run and concatenates strings directly, leaving the system vulnerable to **SQL Injection Attacks**.
- `PreparedStatement`: Pre-compiles the SQL query template on the database server. Dynamic inputs are passed via typed parameter placeholders (`?`). The database treats input parameters strictly as literal values rather than executable SQL commands, completely neutralizing SQL injection risks. It also improves execution speed for repeated operations via execution plan caching.

### Q12: How are database migrations managed idempotently without external tools?
**Answer:**  
We built `DatabaseInitializer`, which:
1. Connects to MySQL and executes `CREATE DATABASE IF NOT EXISTS smart_hostel_db`.
2. Creates a `schema_version` table if it doesn't already exist.
3. Checks if version `1` has been executed. If not, it parses `V1__initial_schema.sql` statement by statement and records the completion timestamp.
4. On subsequent application startups, the check returns `true` and the script is safely skipped without altering existing data.

---

## 🛡️ Section 4: Security & Best Practices

### Q13: Why did you use PBKDF2 instead of MD5 or SHA-256 for passwords?
**Answer:**  
MD5 and vanilla SHA-256 are designed to be fast cryptographic checksums. However, because modern GPUs can compute billions of SHA-256 hashes per second, attackers can crack stolen passwords using pre-computed rainbow tables and brute force in seconds.  
**PBKDF2 (Password-Based Key Derivation Function 2)** with HMAC-SHA256 is intentionally computationally intensive:
- It applies **65,536 iterations** of hashing, making brute-force cracking prohibitively slow.
- It generates a cryptographically secure 16-byte random **salt** for every user, ensuring identical passwords generate different hash strings and defeating rainbow table attacks.

### Q14: What is a timing attack, and how does your code prevent it?
**Answer:**  
When comparing two strings or byte arrays using standard equality (`equals` or `==`), comparison stops at the first mismatching character. An attacker can measure nanosecond response differences to deduce password hashes character by character.  
Our `PasswordUtil.slowEquals()` performs a constant-time comparison using bitwise XOR (`^`) across the entire byte array regardless of where differences occur:
```java
int diff = expected.length ^ actual.length;
for (int i = 0; i < expected.length && i < actual.length; i++) {
    diff |= expected[i] ^ actual[i];
}
return diff == 0;
```

---

## 🖥️ Section 5: JavaFX Desktop Architecture

### Q15: Why does the application have a `Launcher.java` separate from `MainApp.java`?
**Answer:**  
In Java 11 and newer, running a modular JavaFX application from an executable uber-JAR can trigger the error:  
`"Error: JavaFX runtime components are missing, and are required to run this application"`.  
This happens when the main class extends `javafx.application.Application`. By creating a standard bootstrap class `Launcher` whose `main()` method simply delegates to `MainApp.main(args)` without extending `Application`, the JVM loads all shaded JavaFX runtime libraries onto the standard classpath before JavaFX initializes.

### Q16: How does JavaFX handle threading, and what is `Platform.runLater()`?
**Answer:**  
JavaFX maintains a single UI thread called the **JavaFX Application Thread**. Any modification to visual UI nodes must take place on this thread. If a background worker or database operation needs to update a label, show an alert, or refresh a table, it cannot touch the UI directly without causing a `CalledFromWrongThreadException`.  
Calling `Platform.runLater(Runnable)` queues the update event onto the JavaFX event dispatch queue to execute safely on the UI thread.
