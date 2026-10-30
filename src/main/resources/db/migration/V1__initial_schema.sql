-- Smart Hostel Management System — Schema V1
-- This migration creates all required tables from scratch.
-- It uses IF NOT EXISTS so it is safe to re-run.

-- =====================================================
-- SCHEMA VERSION TRACKING
-- =====================================================
CREATE TABLE IF NOT EXISTS schema_version (
    version     INT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    applied_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- USERS (Authentication & Roles)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100),
    phone         VARCHAR(20),
    role          ENUM('ADMIN','WARDEN','STUDENT') NOT NULL,
    active        BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB;

-- =====================================================
-- STUDENTS (extends users)
-- =====================================================
CREATE TABLE IF NOT EXISTS students (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL UNIQUE,
    register_number VARCHAR(30) NOT NULL UNIQUE,
    department      VARCHAR(100) NOT NULL,
    year            INT NOT NULL,
    guardian_name   VARCHAR(100),
    guardian_phone  VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_students_dept (department),
    INDEX idx_students_reg (register_number)
) ENGINE=InnoDB;

-- =====================================================
-- STAFF (wardens and other hostel staff)
-- =====================================================
CREATE TABLE IF NOT EXISTS staff (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL UNIQUE,
    designation     VARCHAR(100),
    assigned_block_id INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================
-- HOSTEL HIERARCHY: Hostel → Block → Floor → Room → Bed
-- =====================================================
CREATE TABLE IF NOT EXISTS hostels (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    address    VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blocks (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    hostel_id  INT NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_block_hostel (hostel_id, name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS floors (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    block_id     INT NOT NULL,
    floor_number INT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (block_id) REFERENCES blocks(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_floor_block (block_id, floor_number)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rooms (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    floor_id    INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type   ENUM('SINGLE','DOUBLE','TRIPLE','DORMITORY') NOT NULL DEFAULT 'DOUBLE',
    capacity    INT NOT NULL DEFAULT 2,
    active      BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (floor_id) REFERENCES floors(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_room_floor (floor_id, room_number)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS beds (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    room_id     INT NOT NULL,
    bed_number  VARCHAR(10) NOT NULL,
    status      ENUM('AVAILABLE','OCCUPIED','UNDER_MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_bed_room (room_id, bed_number)
) ENGINE=InnoDB;

-- Add FK for staff.assigned_block_id now that blocks table exists
ALTER TABLE staff ADD CONSTRAINT fk_staff_block
    FOREIGN KEY (assigned_block_id) REFERENCES blocks(id) ON DELETE SET NULL;

-- =====================================================
-- ALLOCATIONS (student ↔ bed assignment)
-- =====================================================
CREATE TABLE IF NOT EXISTS allocations (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    student_record_id   INT NOT NULL,
    bed_id              INT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    status              ENUM('ACTIVE','TRANSFERRED','VACATED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    allocated_by_user_id INT NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_record_id) REFERENCES students(id) ON DELETE RESTRICT,
    FOREIGN KEY (bed_id) REFERENCES beds(id) ON DELETE RESTRICT,
    FOREIGN KEY (allocated_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_alloc_student (student_record_id),
    INDEX idx_alloc_bed (bed_id),
    INDEX idx_alloc_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- LEAVE REQUESTS
-- =====================================================
CREATE TABLE IF NOT EXISTS leave_requests (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    student_record_id   INT NOT NULL,
    departure_date      DATE NOT NULL,
    return_date         DATE NOT NULL,
    reason              TEXT NOT NULL,
    status              ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    decided_by_user_id  INT,
    decision_remarks    TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_record_id) REFERENCES students(id) ON DELETE RESTRICT,
    FOREIGN KEY (decided_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_leave_student (student_record_id),
    INDEX idx_leave_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- COMPLAINTS
-- =====================================================
CREATE TABLE IF NOT EXISTS complaints (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    student_record_id     INT NOT NULL,
    category              ENUM('WATER_SUPPLY','ELECTRICITY','PLUMBING','WIFI','CLEANING','FURNITURE','OTHER') NOT NULL,
    description           TEXT NOT NULL,
    location              VARCHAR(200),
    priority              ENUM('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT 'MEDIUM',
    recommended_priority  ENUM('LOW','MEDIUM','HIGH','CRITICAL'),
    priority_score        DOUBLE DEFAULT 0,
    priority_explanation  TEXT,
    status                ENUM('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    assigned_to_user_id   INT,
    resolution_notes      TEXT,
    urgency               INT DEFAULT 3,
    impact_count          INT DEFAULT 1,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_record_id) REFERENCES students(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_complaint_status (status),
    INDEX idx_complaint_priority (priority),
    INDEX idx_complaint_student (student_record_id)
) ENGINE=InnoDB;

-- =====================================================
-- COMPLAINT HISTORY (audit trail for status changes)
-- =====================================================
CREATE TABLE IF NOT EXISTS complaint_history (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id    INT NOT NULL,
    old_status      VARCHAR(20),
    new_status      VARCHAR(20) NOT NULL,
    changed_by_user_id INT,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- ATTENDANCE
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    student_record_id   INT NOT NULL,
    attendance_date     DATE NOT NULL,
    status              ENUM('PRESENT','ABSENT','ON_LEAVE') NOT NULL,
    recorded_by_user_id INT NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_record_id) REFERENCES students(id) ON DELETE RESTRICT,
    FOREIGN KEY (recorded_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_attendance_student_date (student_record_id, attendance_date)
) ENGINE=InnoDB;

-- =====================================================
-- FEE OBLIGATIONS & PAYMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS fee_records (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    student_record_id   INT NOT NULL,
    description         VARCHAR(200) NOT NULL,
    total_amount        DECIMAL(10,2) NOT NULL,
    paid_amount         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status              ENUM('UNPAID','PARTIALLY_PAID','PAID') NOT NULL DEFAULT 'UNPAID',
    due_date            DATE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_record_id) REFERENCES students(id) ON DELETE RESTRICT,
    INDEX idx_fee_student (student_record_id),
    INDEX idx_fee_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    fee_record_id       INT NOT NULL,
    amount              DECIMAL(10,2) NOT NULL,
    payment_date        DATE NOT NULL,
    payment_method      VARCHAR(50) NOT NULL,
    reference_number    VARCHAR(100),
    recorded_by_user_id INT NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fee_record_id) REFERENCES fee_records(id) ON DELETE RESTRICT,
    FOREIGN KEY (recorded_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_payment_fee (fee_record_id)
) ENGINE=InnoDB;

-- =====================================================
-- NOTICES
-- =====================================================
CREATE TABLE IF NOT EXISTS notices (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    body            TEXT NOT NULL,
    audience        ENUM('ALL','STUDENTS','WARDENS') NOT NULL DEFAULT 'ALL',
    publish_date    DATE NOT NULL,
    expiry_date     DATE,
    posted_by_user_id INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_notice_audience (audience),
    INDEX idx_notice_dates (publish_date, expiry_date)
) ENGINE=InnoDB;

-- =====================================================
-- WAITING LIST (FIFO queue, persisted in MySQL)
-- =====================================================
CREATE TABLE IF NOT EXISTS waiting_list (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    student_record_id   INT NOT NULL,
    preferred_block     VARCHAR(50),
    preferred_room_type VARCHAR(20),
    preferred_floor     INT DEFAULT -1,
    fulfilled           BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_record_id) REFERENCES students(id) ON DELETE RESTRICT,
    INDEX idx_wl_fulfilled (fulfilled)
) ENGINE=InnoDB;

-- =====================================================
-- AUDIT LOG
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   INT,
    details     TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB;
