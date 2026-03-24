# Kitchen Order Ticket (KOT) System

A robust, multithreaded restaurant management application built using Core Java and Swing GUI, implementing a Producer-Consumer threading model and JDBC integration with MySQL to digitize the flow between floor staff and the kitchen.

## Features
- **Waiters (Producers)**: Send orders safely to a synchronized, bounded shared queue.
- **Chefs (Consumers)**: Pick pending tasks from the queue to process or cancel.
- **Owner Dashboard**: Live tracking, immutable read-only dashboards, and active staff management.
- **MySQL Integration**: Loads 60 pre-seeded food items and logs all successfully processed orders into the database for secure auditing.

## Setup Instructions

### Prerequisites
1. **Java JDK 11+**
2. **Maven**
3. **MySQL Server** running on `localhost:3306`

### Database Setup
To initialize the application, create the database and log tables beforehand. Run the provided script:
```bash
mysql -u root -p < src/main/resources/schema.sql
```
*(Make sure to use your correct MySQL password and ensure your `DatabaseConnection.java` matches your local username/password configuration if it differs from root/password).*

### Building and Running
1. Open terminal inside the project root: `/home/jay/Documents/javaProjects/kitchenOrderTicketKOT`
2. Compile and package the application using Maven:
```bash
mvn clean install
```
3. Run the application through Maven using `exec:java`:
```bash
mvn exec:java -Dexec.mainClass="com.kot.Main"
```

## Multithreaded Architecture (Producer - Consumer)
- The core of KOT is driven by `SharedQueue.java`.
- When Waiters place an order, they launch a Producer Thread mimicking `addOrder()` which uses a thread-safe `wait()` and `notifyAll()` structure, handling the `KitchenOverloadException` if the thread boundaries are met.
- Chefs individually bind themselves to background Consumer Threads that poll `getNextOrder()`, sleeping when the kitchen has nothing to instruct until awoken.
