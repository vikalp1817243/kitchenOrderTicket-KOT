package com.kot.db;

import com.kot.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class UserDAO {

    public User authenticate(String username, String password, String role) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getBoolean("is_first_login")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication failed DB error: " + e.getMessage());
        }
        return null;
    }

    public String[] createUser(String name, String role) {
        // Generate pseudo-random username and password
        String baseUser = role.toLowerCase() + "_" + name.toLowerCase().replaceAll("\\s+", "");
        String username = baseUser + "_" + (new Random().nextInt(900) + 100);
        String tempPassword = "pass" + (new Random().nextInt(9000) + 1000);

        String query = "INSERT INTO users (name, username, password, role, is_first_login) VALUES (?, ?, ?, ?, TRUE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, tempPassword);
            stmt.setString(4, role);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return new String[]{username, tempPassword};
            }
        } catch (SQLException e) {
            System.err.println("Failed to create user: " + e.getMessage());
        }
        return null;
    }

    public boolean updatePassword(int userId, String newPassword) {
        String query = "UPDATE users SET password = ?, is_first_login = FALSE WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update password: " + e.getMessage());
        }
        return false;
    }
}
