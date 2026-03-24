package com.kot.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to provide JDBC Connection.
 */
public class DatabaseConnection {
    // Modify these according to your local MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/kitchen_order_ticket";
    private static final String USER = "root";
    private static final String PASSWORD = "5=0MrZ(@:)MO";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            throw new SQLException(e);
        }
    }
}
