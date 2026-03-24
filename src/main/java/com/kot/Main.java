package com.kot;

import com.kot.core.SharedQueue;
import com.kot.db.MenuDAO;
import com.kot.model.Chef;
import com.kot.model.MenuItem;
import com.kot.model.Waiter;
import com.kot.ui.gui.LoginWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Main {
    private static final int MAX_QUEUE_CAPACITY = 50;

    public static void main(String[] args) {
        // Run completely on the EDT
        SwingUtilities.invokeLater(() -> {
            
            // 1. Initialize core multithreading shared queue
            SharedQueue sharedQueue = new SharedQueue(MAX_QUEUE_CAPACITY);
            
            // 2. Load DB Menu once for fast lookups
            MenuDAO menuDAO = new MenuDAO();
            Map<Integer, MenuItem> menuData = menuDAO.loadMenu();
            
            if (menuData.isEmpty()) {
                JOptionPane.showMessageDialog(null, 
                    "No menu items loaded. Did you run schema.sql?", 
                    "DB Warning", JOptionPane.WARNING_MESSAGE);
            }

            // 3. Launch the secure startup screen (LoginWindow)
            LoginWindow loginWindow = new LoginWindow(sharedQueue, menuData);
            loginWindow.setVisible(true);
        });
    }
}
