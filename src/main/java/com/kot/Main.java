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

            // 3. Create persistent Launcher UI
            JFrame launcher = new JFrame("KOT Global Launcher");
            launcher.setSize(400, 200);
            launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            launcher.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Welcome to Kitchen Order Ticket System!", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            
            JButton newLoginBtn = new JButton("Open New Login Window");
            newLoginBtn.setFont(new Font("Arial", Font.BOLD, 16));
            
            newLoginBtn.addActionListener(e -> {
                // Spawn a new independent Login screen within the same JVM memory
                LoginWindow loginWindow = new LoginWindow(sharedQueue, menuData);
                loginWindow.setVisible(true);
            });
            
            panel.add(titleLabel);
            panel.add(new JLabel("Keep this launcher open to spawn multiple sessions.", SwingConstants.CENTER));
            panel.add(newLoginBtn);
            
            launcher.add(panel);
            launcher.setVisible(true);
            
            // Auto open the first login window
            new LoginWindow(sharedQueue, menuData).setVisible(true);
        });
    }
}
