# Kitchen Order Ticket (KOT) System - Project Log

This document tracks the major design challenges, bugs, and edge cases encountered during the development of the Kitchen Order Ticket system, along with the sequential solutions applied.

---

### 1. Waiter Overloading the Kitchen (Producer Over-production)
**Problem:** Waiters (Producers) could potentially push hundreds of orders into the `SharedQueue`, overwhelming the Chefs and overflowing memory.
**Solution:** A strict queue limit was enforced using a custom `KitchenOverloadException`. If the queue reaches maximum capacity, Waiter threads are safely blocked or warned via GUI that they cannot transmit further orders.

```java
// SharedQueue.java
public synchronized void addOrder(Order order) throws KitchenOverloadException {
    if (queue.size() >= MAX_CAPACITY) {
        throw new KitchenOverloadException("Kitchen queue is full. Cannot accept more orders right now.");
    }
    queue.offer(order);
    System.out.println("Order added to queue. Total pending: " + queue.size());
    notifyAll(); // Wake up any waiting Chefs
}
```

---

### 2. Lack of GUI Event Synchronization
**Problem:** A Chef could mark an order as `COMPLETED`, but neither the Owner's live dashboard nor the Waiter's interface would automatically know about it.
**Solution:** We implemented the Observer pattern via an `OrderUpdateListener`. `SharedQueue` holds a list of listeners (the active Waiter and Owner windows). When an order status changes, it loops through and invokes `onOrderCompleted`.

```java
// SharedQueue.java
public void completeOrder(Order order, String chefName) {
    // ... update DB ...
    for (OrderUpdateListener listener : listeners) {
        listener.onOrderCompleted(order); // UI auto-refreshes
    }
}
```

---

### 3. JVM Memory Isolation (Multi-Terminal Desync)
**Problem:** When running the application in 3 completely separate terminal windows (`java com.kot.Main`), 3 separate Java Virtual Machines (JVMs) were created. The `SharedQueue` only exists in local memory. Thus, Waiter 1 was pushing to Terminal 1's memory, while Chef 1 was waiting on Terminal 2's empty memory. The multithreading `wait()` and `notifyAll()` logic failed to bridge the gap.
**Solution:** We preserved the syllabus requirements (in-memory multi-threading) by converting `Main.java` into a persistent **KOT Global Launcher**. Users only run the program *once* from one terminal, and can click "Open New Login Window" to spawn multiple GUI instances within the *exact same JVM process*, achieving perfect synchronization.

```java
// Main.java
newLoginBtn.addActionListener(e -> {
    // Spawns a new independent Login screen sharing the same JVM memory/SharedQueue
    LoginWindow loginWindow = new LoginWindow(sharedQueue, menuData);
    loginWindow.setVisible(true);
});
```

---

### 4. Edge Case: Removing an Actively Working Chef
**Problem:** The Owner had the ability to remove a Chef via the "Manage Staff" tab. However, if the Chef was already in the middle of processing an order, their database connection would be interrupted, and their GUI window would frustratingly remain "stuck" on screen. 
**Solution:** Two fixes were introduced:
1. We gave `OwnerWindow` the power to physically call `disposeWindow()` on the target employee's frame to forcefully log them out visually.
2. We added a safety check: if the target Chef is actively cooking (`c.getCurrentOrder() != null`), the Owner is blocked from terminating them mid-shift.

```java
// OwnerWindow.java - removeSelectedStaff()
ChefWindow c = SessionManager.getInstance().getChefWindow(id);
if (c != null) {
    if (c.getCurrentOrder() != null) {
        JOptionPane.showMessageDialog(this, "Cannot remove: Chef is currently processing an order! Wait until they finish.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
        return;
    }
    c.disposeWindow(); // Force close Chef's GUI remotely 
    JOptionPane.showMessageDialog(this, "Chef " + id + " has been logged out and removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
}
```

---

### 5. Session Duplication
**Problem:** Without restrictions, the same user (e.g., `Chef 1`) could log in simultaneously from 5 different login windows, breaking data integrity and causing duplicate thread consumption.
**Solution:** Introduced a strict `loggedInUserIds` list inside the `SessionManager` Singleton.

```java
// LoginWindow.java
if (SessionManager.getInstance().isUserLoggedIn(user.getUserId())) {
    JOptionPane.showMessageDialog(this, "This user is already logged in actively! (Close other window first)", "Login Failed", JOptionPane.ERROR_MESSAGE);
    return;
}
```
