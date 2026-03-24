package com.kot.db;

import com.kot.model.MenuItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles operations related to `menu_items`.
 */
public class MenuDAO {

    /**
     * Loads all menu items from the database into a memory HashMap for fast O(1) lookups.
     */
    public Map<Integer, MenuItem> loadMenu() {
        Map<Integer, MenuItem> menu = new HashMap<>();
        String query = "SELECT * FROM menu_items";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int code = rs.getInt("item_code");
                String name = rs.getString("item_name");
                String category = rs.getString("category");
                double price = rs.getDouble("price");
                String portion = rs.getString("portion");

                menu.put(code, new MenuItem(code, name, category, price, portion));
            }
            System.out.println("Loaded " + menu.size() + " menu items from Database.");
        } catch (SQLException e) {
            System.err.println("Failed to load menu items: " + e.getMessage());
            System.err.println("Make sure MySQL is running and you have created the kitchen_order_ticket database using schema.sql.");
        }

        return menu;
    }
}
