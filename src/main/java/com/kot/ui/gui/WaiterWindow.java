package com.kot.ui.gui;

import com.kot.core.SessionManager;
import com.kot.core.SharedQueue;
import com.kot.event.OrderUpdateListener;
import com.kot.exception.KitchenOverloadException;
import com.kot.model.MenuItem;
import com.kot.model.Order;
import com.kot.model.Waiter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaiterWindow extends JFrame implements OrderUpdateListener {
    private final Waiter waiter;
    private final SharedQueue sharedQueue;
    private final Map<Integer, MenuItem> menuData;
    
    // Stats Dashboard
    private int ordersCompletedCount = 0;
    private int ordersRejectedCount = 0;
    private double totalRevenueGenerated = 0.0;
    
    // UI Components
    private JLabel statsLabel;
    private DefaultListModel<String> currentOrderListModel;
    private List<MenuItem> currentOrderItems;
    private JTextField tableNumberField;

    public WaiterWindow(Waiter waiter, SharedQueue sharedQueue, Map<Integer, MenuItem> menuData) {
        this.waiter = waiter;
        this.sharedQueue = sharedQueue;
        this.menuData = menuData;
        this.currentOrderItems = new ArrayList<>();
        
        SessionManager.getInstance().registerWaiter(this);
        SessionManager.getInstance().registerSession(waiter.getEmployeeId());
        sharedQueue.addOrderUpdateListener(this);
        
        setTitle("Waiter Terminal - " + waiter.getName() + " (ID: " + waiter.getEmployeeId() + ")");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
    }
    
    public Waiter getWaiter() {
        return waiter;
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("New Order", createOrderPanel());
        tabbedPane.addTab("My Dashboard", createDashboardPanel());
        add(tabbedPane);
    }
    
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top: Table Number
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Table Number:"));
        tableNumberField = new JTextField(5);
        topPanel.add(tableNumberField);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center: Menu Selection & Current Order
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Menu List
        DefaultListModel<MenuItem> menuListModel = new DefaultListModel<>();
        for (MenuItem item : menuData.values()) {
            menuListModel.addElement(item);
        }
        JList<MenuItem> menuList = new JList<>(menuListModel);
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBorder(BorderFactory.createTitledBorder("Full Menu"));
        centerPanel.add(menuScroll);
        
        // Current Order List
        currentOrderListModel = new DefaultListModel<>();
        JList<String> orderList = new JList<>(currentOrderListModel);
        JScrollPane orderScroll = new JScrollPane(orderList);
        orderScroll.setBorder(BorderFactory.createTitledBorder("Current Order"));
        centerPanel.add(orderScroll);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom: Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addItemBtn = new JButton("Add Item ->");
        JButton placeOrderBtn = new JButton("Push Kitchen Order (KOT)");
        
        addItemBtn.addActionListener(e -> {
            MenuItem selected = menuList.getSelectedValue();
            if (selected != null) {
                currentOrderItems.add(selected);
                currentOrderListModel.addElement(selected.getItemName() + " - " + selected.getPrice());
            }
        });
        
        placeOrderBtn.addActionListener(e -> placeOrder());
        
        bottomPanel.add(addItemBtn);
        bottomPanel.add(placeOrderBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void placeOrder() {
        if (currentOrderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order is empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int tableNum;
        try {
            tableNum = Integer.parseInt(tableNumberField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Table Number!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Order newOrder = new Order(waiter.getName(), tableNum, new ArrayList<>(currentOrderItems));
        
        // Multi-threading: Waiter acts as producer.
        Thread producerThread = new Thread(() -> {
            try {
                sharedQueue.addOrder(newOrder); // Synchronized wait/notify
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Order #" + newOrder.getOrderId() + " pushed to queue successfully!");
                    currentOrderItems.clear();
                    currentOrderListModel.clear();
                    tableNumberField.setText("");
                });
            } catch (KitchenOverloadException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Queue Full", JOptionPane.WARNING_MESSAGE);
                });
            }
        });
        producerThread.start();
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
        logoutBtn.addActionListener(e -> dispose());
        panel.add(logoutBtn);
        
        return panel;
    }
    
    private String getStatsText() {
        return String.format("<html>Orders Completed: <font color='green'>%d</font><br>" +
                             "Orders Rejected: <font color='red'>%d</font><br>" +
                             "Total Revenue Generated: <font color='blue'>₹%.2f</font></html>", 
                             ordersCompletedCount, ordersRejectedCount, totalRevenueGenerated);
    }
    
    private void updateDashboard() {
        SwingUtilities.invokeLater(() -> {
            statsLabel.setText(getStatsText());
        });
    }

    @Override
    public void onOrderCompleted(Order order) {
        if (order.getWaiterName().equals(this.waiter.getName())) {
            ordersCompletedCount++;
            totalRevenueGenerated += order.getTotalAmount();
            updateDashboard();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Your Order #" + order.getOrderId() + " for Table " + order.getTableNumber() + " is ready to serve!", "Order Completed", JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    @Override
    public void onOrderCancelled(Order order) {
        if (order.getWaiterName().equals(this.waiter.getName())) {
            ordersRejectedCount++;
            updateDashboard();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Your Order #" + order.getOrderId() + " was REJECTED by " + order.getChefName() + "!\nReason: " + order.getRejectionReason(), "Order Rejected", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    public void disposeWindow() {
        sharedQueue.removeOrderUpdateListener(this);
        this.dispose();
    }
    
    @Override
    public void dispose() {
        sharedQueue.removeOrderUpdateListener(this);
        SessionManager.getInstance().unregisterWaiter(this);
        SessionManager.getInstance().unregisterSession(waiter.getEmployeeId());
        super.dispose();
    }
}
