# Database Schema & Relational Model
## Smart Hostel Management System (`smart_hostel_db`)

---

## 1. Overview & Technical Specs

- **Database Engine:** MySQL 8.0+ (InnoDB Storage Engine)
- **Default Character Set:** `utf8mb4` (full multilingual & emoji support)
- **Collation:** `utf8mb4_unicode_ci`
- **ACID Support:** Strict transactional consistency with foreign key constraints, unique indexes, and row-level locking (`SELECT FOR UPDATE`).
- **Migration Strategy:** Automated versioned migrations managed by `DatabaseInitializer` using the `schema_version` tracking table. No manual table generation required.

---

## 2. Mermaid Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    users ||--o| students : "extends"
    users ||--o| wardens : "extends"
    users ||--o{ notices : "authors"
    users ||--o{ audit_logs : "triggers"
    users ||--o{ complaint_history : "updates"

    hostels ||--o{ blocks : "contains"
    blocks ||--o{ floors : "contains"
    floors ||--o{ rooms : "contains"
    rooms ||--o{ beds : "contains"

    students ||--o{ allocations : "holds"
    beds ||--o{ allocations : "assigned_to"

    students ||--o{ complaints : "files"
    complaints ||--o{ complaint_history : "tracks"

    students ||--o{ leave_requests : "submits"
    students ||--o{ attendance : "logs"
    students ||--o{ fee_records : "billed"
    fee_records ||--o{ payments : "receives"
    students ||--o{ waiting_list : "enqueued"

    users {
        int id PK
        string username UK
        string password_hash
        string full_name
        string email UK
        string phone
        enum role "ADMIN, WARDEN, STUDENT"
        boolean active
        timestamp created_at
    }

    students {
        int id PK
        int user_id FK
        string register_number UK
        string department
        int year
        string guardian_name
        string guardian_phone
    }

    hostels {
        int id PK
        string name UK
        string code UK
        enum type "BOYS, GIRLS, COED"
        string address
        int total_capacity
    }

    blocks {
        int id PK
        int hostel_id FK
        string name
        int warden_user_id FK
    }

    floors {
        int id PK
        int block_id FK
        int floor_number
    }

    rooms {
        int id PK
        int floor_id FK
        string room_number
        enum type "SINGLE, DOUBLE, TRIPLE, DORMITORY"
        int capacity
    }

    beds {
        int id PK
        int room_id FK
        string bed_number
        enum status "AVAILABLE, OCCUPIED, UNDER_MAINTENANCE"
    }

    allocations {
        int id PK
        int student_id FK
        int bed_id FK
        date start_date
        date end_date
        enum status "ACTIVE, VACATED, CANCELLED"
        string remarks
    }

    complaints {
        int id PK
        int student_id FK
        enum category "WATER_SUPPLY, ELECTRICITY, PLUMBING, WIFI, CLEANING, FURNITURE, OTHER"
        string description
        string location
        enum priority "LOW, MEDIUM, HIGH, CRITICAL"
        enum recommended_priority
        double priority_score
        string priority_explanation
        enum status "OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED"
        int assigned_to_user_id FK
        string resolution_notes
        int urgency
        int impact_count
    }

    fee_records {
        int id PK
        int student_id FK
        string fee_type
        decimal amount
        decimal paid_amount
        date due_date
        enum status "UNPAID, PARTIALLY_PAID, PAID"
        string academic_term
    }

    payments {
        int id PK
        int fee_record_id FK
        decimal amount
        date payment_date
        string payment_mode
        string transaction_ref UK
        string receipt_number UK
    }

    leave_requests {
        int id PK
        int student_id FK
        date start_date
        date end_date
        string reason
        string destination
        enum status "PENDING, APPROVED, REJECTED, CANCELLED"
        int approved_by_user_id FK
    }

    attendance {
        int id PK
        int student_id FK
        date attendance_date
        enum status "PRESENT, ABSENT, ON_LEAVE, LATE"
        string remarks
    }

    waiting_list {
        int id PK
        int student_id FK
        string preferred_block
        string preferred_room_type
        int preferred_floor
        boolean fulfilled
        timestamp created_at
    }
```

---

## 3. Relational Data Dictionary

### 1. `schema_version`
Tracks versioned migration scripts to ensure startup scripts are idempotent.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `version` | INT | PRIMARY KEY | Sequential migration number |
| `description` | VARCHAR(255) | NOT NULL | Human-readable migration description |
| `applied_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Date and time executed |

### 2. `users`
Master table for all system identities.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Unique user identifier |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | Login credential |
| `password_hash` | VARCHAR(255) | NOT NULL | PBKDF2WithHmacSHA256 salted hash |
| `full_name` | VARCHAR(100) | NOT NULL | Full display name |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | Contact email address |
| `phone` | VARCHAR(20) | NULL | 10-12 digit mobile number |
| `role` | ENUM | NOT NULL | `ADMIN`, `WARDEN`, or `STUDENT` |
| `active` | BOOLEAN | DEFAULT TRUE | Account state |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account registration date |

### 3. `hostels`
Hostel buildings on campus.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Unique hostel ID |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | Hostel name (e.g., Everest Boys Hostel) |
| `code` | VARCHAR(20) | NOT NULL, UNIQUE | Short code (e.g., EBH) |
| `type` | ENUM | NOT NULL | `BOYS`, `GIRLS`, or `COED` |
| `address` | VARCHAR(255) | NULL | Campus location |
| `total_capacity`| INT | DEFAULT 0 | Maximum accommodation capacity |

### 4. `blocks`
Physical wings or blocks within a hostel.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Block identifier |
| `hostel_id` | INT | NOT NULL, FK(`hostels.id`) | Owning hostel |
| `name` | VARCHAR(50) | NOT NULL | Block name (e.g., Block A, East Wing) |
| `warden_user_id`| INT | NULL, FK(`users.id`) | Assigned supervisor warden |

### 5. `floors`
Floors within a block.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Floor identifier |
| `block_id` | INT | NOT NULL, FK(`blocks.id`) | Owning block |
| `floor_number` | INT | NOT NULL | Floor index (0 = Ground, 1 = 1st) |

### 6. `rooms`
Rooms located on a floor.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Room identifier |
| `floor_id` | INT | NOT NULL, FK(`floors.id`) | Floor reference |
| `room_number` | VARCHAR(20) | NOT NULL | Room code (e.g., 101, 204B) |
| `room_type` | ENUM | NOT NULL | `SINGLE`, `DOUBLE`, `TRIPLE`, `DORMITORY` |
| `capacity` | INT | NOT NULL | Maximum beds supported |

### 7. `beds`
The atomic allocatable resource in the system.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Bed identifier |
| `room_id` | INT | NOT NULL, FK(`rooms.id`) | Room reference |
| `bed_number` | VARCHAR(20) | NOT NULL | Bed code (e.g., Bed-1, Bed-A) |
| `status` | ENUM | NOT NULL | `AVAILABLE`, `OCCUPIED`, `UNDER_MAINTENANCE` |

### 8. `students`
Student-specific profile table.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Student profile identifier |
| `user_id` | INT | NOT NULL, UNIQUE, FK(`users.id`) | Linked authentication account |
| `register_number`| VARCHAR(50) | NOT NULL, UNIQUE | University register number |
| `department` | VARCHAR(100) | NOT NULL | Academic branch (e.g. CSE, ECE) |
| `year` | INT | NOT NULL | Current academic year (1-4) |
| `guardian_name` | VARCHAR(100) | NULL | Parent/guardian name |
| `guardian_phone`| VARCHAR(20) | NULL | Emergency guardian contact |

### 9. `allocations`
Tracks bed tenancy records and room allocations.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Allocation identifier |
| `student_id` | INT | NOT NULL, FK(`students.id`) | Student occupant |
| `bed_id` | INT | NOT NULL, FK(`beds.id`) | Assigned bed |
| `start_date` | DATE | NOT NULL | Check-in / allocation date |
| `end_date` | DATE | NULL | Check-out date |
| `status` | ENUM | NOT NULL | `ACTIVE`, `VACATED`, `CANCELLED` |
| `remarks` | TEXT | NULL | Special conditions / remarks |

### 10. `complaints`
Student grievances with rule-based AI triage scoring.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | AUTO_INCREMENT, PK | Complaint ticket identifier |
| `student_id` | INT | NOT NULL, FK(`students.id`) | Submitting resident |
| `category` | ENUM | NOT NULL | `WATER_SUPPLY`, `ELECTRICITY`, `PLUMBING`, etc. |
| `description` | TEXT | NOT NULL | Problem narrative |
| `location` | VARCHAR(100) | NOT NULL | Room/block details |
| `priority` | ENUM | NOT NULL | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `recommended_priority`| ENUM | NULL | AI algorithmic recommendation |
| `priority_score`| DOUBLE | NULL | Calculated urgency/impact score (0-100) |
| `priority_explanation`| TEXT | NULL | Human-readable explanation of AI score |
| `status` | ENUM | NOT NULL | `OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `assigned_to_user_id`| INT | NULL, FK(`users.id`) | Staff assignee |
| `resolution_notes`| TEXT | NULL | Completion or repair report |

---

## 4. Key Relational Integrity & Performance Rules

1. **Foreign Key Cascades:** Deleting a `Hostel` cascades deletions down to `Blocks`, `Floors`, `Rooms`, and `Beds`. Deleting a student never deletes historic financial `payments` or completed `audit_logs`.
2. **Composite Indexes:**
   - `idx_beds_room_status` (`room_id`, `status`): Accelerates finding available beds during room recommendations.
   - `idx_allocations_active` (`student_id`, `status`): Speeds up validation preventing a student from obtaining dual active beds.
   - `idx_attendance_student_date` (`student_id`, `attendance_date`): Accelerates monthly attendance percentage calculations.
3. **Pessimistic Concurrency (`SELECT FOR UPDATE`):** Bed reservations lock the target bed row inside an active transaction to guarantee zero double-bookings under multi-user concurrency.
