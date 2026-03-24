package com.kot.db;

import com.kot.model.Order;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles operations related to `completed_orders`.
 */
public class OrderDAO {

    /**
     * Inserts a completed or cancelled order into the database.
     */
    public synchronized static void saveOrderProcessing(Order order) {
        String query = "INSERT INTO completed_orders (waiter_name, chef_name, table_number, total_amount, status, rejection_reason) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
                       
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, order.getWaiterName());
            stmt.setString(2, order.getChefName());
            stmt.setInt(3, order.getTableNumber());
            stmt.setDouble(4, order.getTotalAmount());
            stmt.setString(5, order.getStatus().name());
            
            if (order.getRejectionReason() != null && !order.getRejectionReason().trim().isEmpty()) {
                stmt.setString(6, order.getRejectionReason());
            } else {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save order to Database: " + e.getMessage());
        }
    }
}
