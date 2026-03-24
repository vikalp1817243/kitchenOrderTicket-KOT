# How to Run Kitchen Order Ticket (KOT) System

This guide outlines the complete, step-by-step process to set up, build, and run the KOT System on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed on your system:
- **Java JDK 11** or higher.
- **Apache Maven** (for dependency management and build).
- **MySQL Server** (running on `localhost:3306`).

---

## Step 1: Database Setup

The application relies on a MySQL database to store orders and menu items.

1. **Start your MySQL server** and open a MySQL client (or terminal).
2. **Create the Database and Tables** by executing the provided schema log.
   Open your terminal and run:
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```
   *(You will be prompted to enter your MySQL root password).*
3. **Database Credentials Check**: 
   The project connects to the database using the default credentials:
   - **URL:** `jdbc:mysql://localhost:3306/kitchen_order_ticket`
   - **Username:** `root`
   - **Password:** `password`
   
   *If your local MySQL credentials differ*, you must update them in the application code before building. Open the following file in your IDE or text editor:
   `src/main/java/com/kot/db/DatabaseConnection.java`
   And modify the credentials accordingly.

---

## Step 2: Build the Project

Once the database is ready, you need to compile the Java code and download its dependencies using Maven.

1. **Open a terminal** and navigate to the project's root directory:
   ```bash
   cd /home/jay/Documents/javaProjects/kitchenOrderTicketKOT
   ```
2. **Run the Maven Build Command**:
   ```bash
   mvn clean install
   ```
   This command will clean any previous builds, compile the source code, download the necessary MySQL JDBC driver, and package the application.

---

## Step 3: Run the Application

With the project successfully built, you can now launch the Kitchen Order Ticket system.

1. **Execute the Main Class** using the Maven exec plugin:
   ```bash
   mvn exec:java -Dexec.mainClass="com.kot.Main"
   ```
2. The UI dashboards (Owner, Chef, Waiter) will open. Follow the on-screen interactions to manage the restaurant flow.

---

## Troubleshooting

- **JDBC Driver Not Found / ClassNotFoundException**:
  Ensure Maven has successfully downloaded the dependencies during `mvn clean install`. Check your internet connection if the download fails.
- **Connection Refused / SQLException**: 
  Verify that your MySQL server is running properly on port 3306 and that the credentials in `DatabaseConnection.java` match your MySQL setup.
