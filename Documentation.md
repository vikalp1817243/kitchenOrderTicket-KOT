# Kitchen Order Ticket (KOT) System - Comprehensive Documentation

## 1. Project Overview
The Kitchen Order Ticket (KOT) System is a robust, multithreaded restaurant management application built using Core Java and the Swing GUI toolkit. It seamlessly digitizes the workflow between the dining floor (Waiters) and the kitchen (Chefs). 

The application utilizes a classic **Producer-Consumer threading model** running entirely in-memory to handle order concurrency, coupled with JDBC integration to MySQL for persistent data tracking, menu management, and user authentication.

---

## 2. System Architecture & Multithreading Model
The core architecture focuses on real-time order processing through threading.

### The Global KOT Launcher
To ensure all users share the same memory space, the application runs as a **Global JVM Launcher** via `Main.java`. A single instance of the program runs in the terminal, and users spawn separate graphical login windows that share the exact same background JVM instance and threads.

### Producer-Consumer Flow
- **Producers (Waiters):** Waiters take orders and push them into the `SharedQueue`. They operate as Producer Threads.
- **Consumers (Chefs):** Chefs monitor the queue for pending orders. When an order arrives, a Chef thread picks it up, transitioning the state from *Pending* to *Processing*, and eventually to *Completed* or *Rejected*.
- **SharedQueue:** A synchronized, bounded in-memory object linking Producers and Consumers. It utilizes thread-safe locking mechanisms like `wait()` and `notifyAll()`.

### Observer Pattern (Event Synchronization)
When a Chef finishes an order in the backend thread, the application uses the **Observer design pattern** via an `OrderUpdateListener` to automatically blast notifications to any subscribed GUI clients (like the Owner Dashboard and Waiter interfaces), eliminating the need for manual refreshing.

---

## 3. Database Structure
The application relies on a MySQL database `kitchen_order_ticket`. The database handles the operational load out-of-the-box via a seeding script (`schema.sql`), loading 60 default menu items alongside a default 'Owner' account.

Key tables:
- `users`: Tracks staff credentials, roles (Owner, Waiter, Chef), and enforces mandatory password changes (`is_first_login`).
- `menu_items`: Centralized repository categorized by cuisines/types (e.g., Starters, Veg/Non-Veg Main Course, Breads, Desserts, Beverages). 
- `completed_orders`: Permanent ledger of all finalized restaurant orders for financial auditing and performance tracking. Tracks parameters including total order amounts, wait times, timestamps, and order status.

---

## 4. Security & Session Management
- **Role-based Authentication:** Three distinct privilege levels (Owner, Chef, Waiter), each spawning a uniquely tailored graphical interface upon successful login.
- **Singleton Session Management:** The `SessionManager` prevents session duplication. A user cannot be logged in to two terminals/windows simultaneously. 
- **Graceful Termination / Remote Logout:** If an Owner deletes a staff member or logs them out, the user's active application frames will remotely dispose themselves (`disposeWindow()`). The application has built-in safety checks protecting users from being deleted while actively processing large order threads.

---

## 5. Module Map
The source code under `com.kot` is strictly compartmentalized:
- **`ui.gui`**: Contains Swing Window controllers (`LoginWindow`, `WaiterWindow`, `ChefWindow`, `OwnerWindow`, `ChangePasswordWindow`).
- **`core`**: Contains the engine logic, multithreading constraints (`SharedQueue`), and session tracking.
- **`db`**: Data Access Objects (`OrderDAO`, `MenuDAO`, `UserDAO`) cleanly encapsulating all JDBC MySQL transactions.
- **`model`**: Standard data entities encapsulating properties (`Order`, `MenuItem`, `User`).
- **`event`**: Event listener interfaces linking the backend threads to the GUI front-end.
- **`exception`**: Custom exceptions managing unexpected states (e.g., `KitchenOverloadException`).

---

## 6. Build and Execution Instructions

### Prerequisites
1. Java JDK 11+
2. Maven
3. MySQL Server running on `localhost:3306`

### Setup Database
```bash
mysql -u root -p < src/main/resources/schema.sql
```

### Compile & Run
Navigate to the project root directory and run the application via Maven:
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.kot.Main"
```

---

## 7. Key Design Solutions & Problem Solving Insights
During development, the following major obstacles were systematically resolved:
1. **Queue Flooding:** Introduced a hard queue limit throwing a `KitchenOverloadException` to prevent the JVM from hitting memory limits if Waiters send excessive inputs.
2. **Multi-Terminal JVM Desync:** Re-engineered the executable flow to be a single "Global Launcher" spawned once, sharing internal `SharedQueue` memory safely among infinite internal Windows. 
3. **Mid-Shift Termination Protection:** Built state-tracking algorithms into the Chef objects. The Owner Dashboard is explicitly blocked from firing or forcefully logging out a Chef if their attached Consumer thread is actively cooking (`c.getCurrentOrder() != null`).
