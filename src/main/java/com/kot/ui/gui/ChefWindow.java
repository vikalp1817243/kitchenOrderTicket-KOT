package com.kot.ui.gui;

import com.kot.core.SessionManager;
import com.kot.core.SharedQueue;
import com.kot.model.Chef;
import com.kot.model.Order;

import javax.swing.*;
import java.awt.*;

public class ChefWindow extends JFrame {
    private final Chef chef;
    private final SharedQueue sharedQueue;
    
    // Stats
    private int itemsCooked = 0;
    private int itemsRejected = 0;
    
    // UI
    private JLabel currentOrderLabel;
    private JButton cookBtn;
    private JButton rejectBtn;
    private JButton startShiftBtn;
    private JLabel statsLabel;
    
    // Threading
    private Thread consumerThread;
    private Order currentOrder = null;
    private final Object pauseLock = new Object();
    private boolean orderProcessingComplete = false;
    private volatile boolean isRunning = false;

    public ChefWindow(Chef chef, SharedQueue sharedQueue) {
        this.chef = chef;
        this.sharedQueue = sharedQueue;
        
        SessionManager.getInstance().registerChef(this);
        SessionManager.getInstance().registerSession(chef.getEmployeeId());
        
        setTitle("Chef Terminal - " + chef.getName() + " (ID: " + chef.getEmployeeId() + ")");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
    }
    
    public Chef getChef() {
        return chef;
    }
    
    public Order getCurrentOrder() {
        return currentOrder;
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Kitchen Queue", createQueuePanel());
        tabbedPane.addTab("My Dashboard", createDashboardPanel());
        add(tabbedPane);
    }
    
    private JPanel createQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        startShiftBtn = new JButton("Start Shift (Connect to Queue)");
        startShiftBtn.addActionListener(e -> startConsumerThread());
        panel.add(startShiftBtn, BorderLayout.NORTH);
        
        currentOrderLabel = new JLabel("<html><i>Waiting to start shift...</i></html>", SwingConstants.CENTER);
        currentOrderLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(currentOrderLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        cookBtn = new JButton("Mark Completed");
        cookBtn.setBackground(new Color(144, 238, 144));
        rejectBtn = new JButton("Reject (Out of Ingredients)");
        rejectBtn.setBackground(new Color(255, 182, 193));
        
        cookBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        
        cookBtn.addActionListener(e -> processOrder(true));
        rejectBtn.addActionListener(e -> processOrder(false));
        
        buttonPanel.add(cookBtn);
        buttonPanel.add(rejectBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void startConsumerThread() {
        startShiftBtn.setEnabled(false);
        currentOrderLabel.setText("Waiting for new orders...");
        isRunning = true;
        
        consumerThread = new Thread(() -> {
            try {
                while (isRunning) {
                    // Consumer pulling from SharedQueue (locks internally, uses wait())
                    currentOrder = sharedQueue.getNextOrder();
                    
                    // Update UI safely
                    SwingUtilities.invokeLater(() -> {
                        currentOrderLabel.setText("<html><b>Current Order:</b><br>" + currentOrder.toString() + "</html>");
                        cookBtn.setEnabled(true);
                        rejectBtn.setEnabled(true);
                    });
                    
                    // Pause loop until Chef interacts with GUI buttons
                    synchronized (pauseLock) {
                        orderProcessingComplete = false;
                        while (!orderProcessingComplete) {
                            pauseLock.wait();
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Chef " + chef.getName() + " thread interrupted/stopped.");
            }
        });
        consumerThread.start();
    }
    
    private void processOrder(boolean isCompleted) {
        if (currentOrder == null) return;
        
        cookBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        
        if (isCompleted) {
            sharedQueue.completeOrder(currentOrder, chef.getName());
            itemsCooked++;
        } else {
            String reason = JOptionPane.showInputDialog(this, "Enter Rejection Reason:", "Reject Order", JOptionPane.WARNING_MESSAGE);
            if (reason == null || reason.trim().isEmpty()) reason = "Out of ingredients/time";
            sharedQueue.cancelOrder(currentOrder, chef.getName(), reason);
            itemsRejected++;
        }
        
        updateDashboard();
        currentOrderLabel.setText("Waiting for next order...");
        currentOrder = null;
        
        // Wake up consumer thread to fetch next order
        synchronized (pauseLock) {
            orderProcessingComplete = true;
            pauseLock.notify();
        }
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        statsLabel = new JLabel(getStatsText());
        statsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(statsLabel);
        
        JLabel infoLabel = new JLabel("<html><i>Note: These stats are read-only and automatically updated.</i></html>");
        panel.add(infoLabel);
        
        JButton logoutBtn = new JButton("Logout & Close");
        logoutBtn.setBackground(new Color(255, 100, 100));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> disposeWindow());
        panel.add(logoutBtn);
        
        return panel;
    }
    
    private String getStatsText() {
        return String.format("<html>Orders Cooked/Completed: <font color='green'>%d</font><br>" +
                             "Orders Rejected: <font color='red'>%d</font></html>", 
                             itemsCooked, itemsRejected);
    }
    
    private void updateDashboard() {
        SwingUtilities.invokeLater(() -> {
            statsLabel.setText(getStatsText());
        });
    }
    
    public void disposeWindow() {
        this.setVisible(false); // Immediate visual feedback
        isRunning = false;
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
        this.dispose();
    }

    @Override
    public void dispose() {
        SessionManager.getInstance().unregisterChef(this);
        SessionManager.getInstance().unregisterSession(chef.getEmployeeId());
        isRunning = false;
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
        super.dispose();
    }
}
