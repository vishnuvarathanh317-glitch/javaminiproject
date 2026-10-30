# End-User Role Navigation Guide
## Smart Hostel Management System — CSE203

---

## 1. Getting Started & Initial Launch

### First-Time Startup
When launching the application on a fresh database:
1. Ensure MySQL is running on port 3306.
2. Launch via `mvn javafx:run` or `java -jar target/smart-hostel-management-1.0-SNAPSHOT.jar`.
3. If no administrator account exists in the database, the **First-Run Setup Wizard** will appear automatically:
   - Enter your preferred Admin Username (e.g., `admin`).
   - Enter your Full Name and Email Address.
   - Enter a secure password (at least 6 characters).
   - Click **"Initialize Master Administrator"**.
4. Once completed, you will be redirected to the **Login Screen**.

### Signing In
- Select your target credentials:
  - **Administrator:** `admin` / `admin123`
  - **Warden:** `warden` / `warden123`
  - **Student:** `rahul` / `student123`
- The system automatically detects your role upon successful PBKDF2 hash verification and routes you to your customized dashboard.

---

## 2. Administrator Role Guide

### A. Dashboard Overview
Upon logging in, the Admin Dashboard presents:
- **Live Institutional KPI Cards:** Total Hostels, Usable & Occupied Beds, Active Residents, and Financial Health.
- **Demo Data Seeding:** A single click on **"Seed Demo Data"** will automatically configure standard hostels (Everest Boys Hostel & Sarojini Girls Hostel), blocks, floors, sample rooms, beds, warden accounts, students, and active maintenance tickets.

### B. Hostel & Hierarchy Management
Navigate to **"Hostel Management"** in the sidebar:
1. **Hostel Management:** View all campus hostels. Click **"+ Add Hostel"** to specify the name, code (e.g. `EBH`), type (`BOYS`, `GIRLS`, `COED`), and address.
2. **Block Management:** Click **"Manage Blocks"** to define wings within a hostel and assign a supervisor warden.
3. **Floor & Room Builder:** Select a block to add floors, define room numbers, room types (`SINGLE`, `DOUBLE`, `TRIPLE`, `DORMITORY`), and maximum capacity.
4. **Bed Status Toggle:** Mark beds as `AVAILABLE`, `OCCUPIED`, or `UNDER_MAINTENANCE` (e.g. during painting or structural repairs).

### C. Student Account Management
Navigate to **"Student Management"**:
- **Search & Filter:** Search by name, register number (`REG...`), or department in real time.
- **Register New Student:** Click **"+ Register New Student"** to enter the student's name, email, register number, department, academic year, and guardian emergency contact details.
- **Account Actions:** Edit profile details, toggle active status, or reset credentials.

### D. User & Credential Administration
Navigate to **"User Accounts"**:
- Create additional Warden or Administrator accounts.
- Reset passwords securely using salted hashing.
- Deactivate users without deleting historical foreign-key records.

### E. Institutional Reports & CSV Export
Navigate to **"Institutional Reports"**:
1. Select the report type:
   - **Hostel Occupancy Report:** Summary of total beds, occupied beds, usable capacity, and utilization percentage.
   - **Active Allocations Report:** Roster of all room residents, bed assignments, and check-in dates.
   - **Maintenance & Grievances Report:** Log of all open/in-progress complaints and AI priority scores.
   - **Fee Ledger Report:** Paid vs. outstanding student balances.
2. Click **"Export to Standard CSV"** to save a UTF-8 BOM CSV file compatible with Microsoft Excel, Google Sheets, or LibreOffice.

---

## 3. Warden Role Guide

### A. Daily Dashboard
The Warden Dashboard aggregates day-to-day operations:
- Total active students in your assigned block.
- Pending leave requests awaiting approval.
- High-priority complaints requiring urgent maintenance attention.

### B. Bed Allocation with AI Recommendation Engine
Navigate to **"Room Allocations"**:
1. Select the student needing a room.
2. Specify preferences:
   - **Preferred Block** (e.g., `Block A`).
   - **Preferred Floor** (e.g., Floor `1` for ground access).
   - **Preferred Room Type** (`SINGLE`, `DOUBLE`, etc.).
3. Click **"Run AI Recommendation Engine"**:
   - The system analyzes all available beds using a `PriorityQueue` scoring algorithm.
   - It displays candidate beds sorted by suitability score with human-readable rationale (e.g., *"Matches block: +25; Matches floor: +15; Lower floor access bonus: +5"*).
4. Click **"Confirm & Allocate Selected Bed"** to assign the room. Concurrency locks ensure that no other warden can claim the bed simultaneously.

### C. Waiting-List Processing
If all rooms in a block are full:
- Click **"Enqueue on Waiting List"** to record the student's request in the FIFO `ArrayDeque` queue.
- When an existing occupant checks out, click **"Process Next Waiting Candidate"**. The system checks the head of the queue, verifies that the candidate has not been assigned another room, and offers the newly freed bed.

### D. Grievance Lifecycle Management
Navigate to **"Complaints"**:
1. View all student grievances sorted by urgency and date.
2. Select a complaint and click **"View AI Justification & History"** to inspect:
   - Category base weight (e.g., Water/Electricity = +35 pts).
   - Student urgency rating (1-5 scale).
   - Population impact (number of students affected).
   - Backlog escalation hours.
3. Click **"Advance Lifecycle Status"**:
   - Advance status from `OPEN` → `ASSIGNED` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`.
   - Enter technician remarks or resolution notes.

### E. Leave Request Processing
Navigate to **"Leave Requests"**:
- View incoming student outing/vacation applications.
- Check destination, reason, and start/end dates.
- Click **"Approve Leave"** or **"Reject Leave"** with mandatory administrative remarks.

### F. Daily Attendance Logging
Navigate to **"Attendance Logging"**:
- Select the block and date.
- The roster of assigned students is populated automatically.
- Bulk-mark or individual toggle status: `PRESENT`, `ABSENT`, `ON_LEAVE`, or `LATE`.
- Click **"Save Attendance Batch"** to commit the records.

### G. Notice Board Announcements
Navigate to **"Campus Notices"**:
- Click **"+ New Notice"** to draft an announcement.
- Choose priority badge (`NORMAL`, `URGENT`, `HIGH_PRIORITY`).
- Target all residents or specific hostel blocks.

---

## 4. Student Resident Role Guide

### A. Student Resident Portal
When a student signs in:
- View assigned hostel name, block, room number, and bed number.
- See roommate names and contact emails.
- Check real-time fee balance and pending payment due dates.

### B. Smart Room Selection
If unallocated:
- Access the **Room Allocation Portal**.
- Submit room type preferences and view recommended beds ranked by the AI matching engine before confirming the booking.

### C. Filing Maintenance Complaints
Navigate to **"Grievance Portal"**:
1. Click **"+ File New Grievance"**.
2. Select domain category: `WATER_SUPPLY`, `ELECTRICITY`, `PLUMBING`, `WIFI`, `CLEANING`, or `FURNITURE`.
3. Set your urgency level on the 1–5 slider.
4. Specify impact: `1` for your personal bed, or `2+` if the whole room or floor is affected.
5. Provide a location (e.g. *"Room 102 bathroom"*) and problem description.
6. The AI engine immediately computes your priority score and displays the recommended category before submission!

### D. Submitting Leave Applications
Navigate to **"My Leave Requests"**:
1. Click **"+ Request Leave"**.
2. Pick start date and return date. (The system validates that the start date is not in the past and the return date is after the start date).
3. Enter travel reason and destination.
4. Submit to immediately alert the block warden. Track status updates (`PENDING` → `APPROVED` / `REJECTED`) live on your portal.

### E. Fee Ledger & Payment Tracking
Navigate to **"Fee Ledger"**:
- Review scheduled hostel fee invoices (Room rent, Mess fee, Maintenance).
- Check total billed amount, paid amounts, and outstanding balance.
- Click **"Record Payment"** to log an offline or online payment reference code and generate a receipt number.
