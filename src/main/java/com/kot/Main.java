package com.kot;

import com.kot.core.SharedQueue;
import com.kot.db.MenuDAO;
import com.kot.model.Chef;
import com.kot.model.MenuItem;
import com.kot.model.Waiter;
import com.kot.ui.gui.ChefWindow;
import com.kot.ui.gui.OwnerWindow;
import com.kot.ui.gui.WaiterWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Main {
    private static final int MAX_QUEUE_CAPACITY = 50;
    private static int employeeIdCounter = 1;

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

            // 3. Create Launcher UI
            JFrame launcher = new JFrame("KOT Control Panel");
            launcher.setSize(400, 300);
            launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            launcher.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JButton openOwnerBtn = new JButton("Open Owner Dashboard (Singleton)");
            JButton openWaiterBtn = new JButton("Open New Waiter Window");
            JButton openChefBtn = new JButton("Open New Chef Window");
            
            // Initialize the single Owner Window
            OwnerWindow ownerWindow = new OwnerWindow(sharedQueue);
            
            openOwnerBtn.addActionListener(e -> {
                if (!ownerWindow.isVisible()) {
                    ownerWindow.setVisible(true);
                } else {
                    ownerWindow.toFront();
                }
            });
            
            openWaiterBtn.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(launcher, "Enter Waiter Name:");
                if (name != null && !name.trim().isEmpty()) {
                    Waiter waiter = new Waiter(employeeIdCounter++, name);
                    WaiterWindow wWindow = new WaiterWindow(waiter, sharedQueue, menuData);
                    wWindow.setVisible(true);
                }
            });
            
            openChefBtn.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(launcher, "Enter Chef Name:");
                if (name != null && !name.trim().isEmpty()) {
                    Chef chef = new Chef(employeeIdCounter++, name);
                    ChefWindow cWindow = new ChefWindow(chef, sharedQueue);
                    cWindow.setVisible(true);
                }
            });
            
            panel.add(new JLabel("Welcome to Kitchen Order Ticket System!", SwingConstants.CENTER));
            panel.add(openOwnerBtn);
            panel.add(openWaiterBtn);
            panel.add(openChefBtn);
            
            launcher.add(panel);
            launcher.setVisible(true);
            
            // Auto open owner window for convenience
            ownerWindow.setVisible(true);
        });
    }
}
