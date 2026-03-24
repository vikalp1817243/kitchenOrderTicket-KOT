package com.kot.ui.gui;

import com.kot.core.SessionManager;
import com.kot.core.SharedQueue;
import com.kot.event.OrderUpdateListener;
import com.kot.model.Chef;
import com.kot.model.Order;
import com.kot.model.Waiter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OwnerWindow extends JFrame implements OrderUpdateListener {
    private final SharedQueue sharedQueue;
    
    // UI
    private DefaultListModel<String> queueListModel;
    private DefaultListModel<String> alertsListModel;
    
    // Manage Staff UI
    private DefaultListModel<String> staffListModel;
    private JList<String> staffList;
    
    // Performance Tab UI
    private JTextArea performanceArea;

    // Data
    private int totalOrdersCompleted = 0;
    private int totalOrdersRejected = 0;
    private double totalRevenue = 0.0;

    public OwnerWindow(SharedQueue sharedQueue) {
        this.sharedQueue = sharedQueue;
        sharedQueue.addOrderUpdateListener(this);
        
        setTitle("Owner Dashboard (Live Tracking)");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Closing owner dashboard closes app
        setLocationRelativeTo(null);
        
        initUI();
        
        // Timer to auto-refresh live pending queue and staff list every 2 seconds
        Timer refreshTimer = new Timer(2000, e -> {
            refreshPendingQueue();
            refreshStaffList();
        });
        refreshTimer.start();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Live Kitchen Tracking", createLivePanel());
        tabbedPane.addTab("Overall Performance", createPerformancePanel());
        tabbedPane.addTab("Manage Staff", createManageStaffPanel());
        add(tabbedPane);
    }
    
    private JPanel createLivePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Pending Queue
        queueListModel = new DefaultListModel<>();
        JList<String> queueList = new JList<>(queueListModel);
        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setBorder(BorderFactory.createTitledBorder("Pending Kitchen Queue"));
        panel.add(queueScroll);
        
        // Alerts
        alertsListModel = new DefaultListModel<>();
        JList<String> alertsList = new JList<>(alertsListModel);
        alertsList.setForeground(Color.RED);
        JScrollPane alertsScroll = new JScrollPane(alertsList);
        alertsScroll.setBorder(BorderFactory.createTitledBorder("Cancelled / Rejected Alerts"));
        panel.add(alertsScroll);
        
        return panel;
    }
    
    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        performanceArea = new JTextArea("Initializing performance stats...");
        performanceArea.setEditable(false);
        performanceArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(performanceArea), BorderLayout.CENTER);
        
        updatePerformanceArea();
        return panel;
    }
    
    private JPanel createManageStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        staffListModel = new DefaultListModel<>();
        staffList = new JList<>(staffListModel);
        panel.add(new JScrollPane(staffList), BorderLayout.CENTER);
        
        JButton removeBtn = new JButton("Remove Selected Staff");
        removeBtn.addActionListener(e -> removeSelectedStaff());
        panel.add(removeBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshPendingQueue() {
        List<Order> snapshot = sharedQueue.getPendingOrdersSnapshot();
        SwingUtilities.invokeLater(() -> {
            queueListModel.clear();
            for (Order o : snapshot) {
                queueListModel.addElement("Order #" + o.getOrderId() + " | Table " + o.getTableNumber() + " | By: " + o.getWaiterName());
            }
        });
    }
    
    private void refreshStaffList() {
        SessionManager sm = SessionManager.getInstance();
        List<Waiter> waiters = sm.getActiveWaitersList();
        List<Chef> chefs = sm.getActiveChefsList();
        
        SwingUtilities.invokeLater(() -> {
            staffListModel.clear();
            for (Waiter w : waiters) {
                staffListModel.addElement("Waiter ID: " + w.getEmployeeId() + " | Name: " + w.getName());
            }
            for (Chef c : chefs) {
                staffListModel.addElement("Chef ID: " + c.getEmployeeId() + " | Name: " + c.getName());
            }
        });
    }
    
    private void removeSelectedStaff() {
        String selection = staffList.getSelectedValue();
        if (selection == null) return;
        
        int id = Integer.parseInt(selection.split("\\|")[0].replaceAll("[^0-9]", ""));
        
        if (selection.startsWith("Waiter")) {
            WaiterWindow w = SessionManager.getInstance().getWaiterWindow(id);
            if (w != null) SessionManager.getInstance().unregisterWaiter(w);
        } else if (selection.startsWith("Chef")) {
            ChefWindow c = SessionManager.getInstance().getChefWindow(id);
            if (c != null) SessionManager.getInstance().unregisterChef(c);
        }
        
        refreshStaffList();
    }
    
    private void updatePerformanceArea() {
        SwingUtilities.invokeLater(() -> {
            String text = "====================================\n" +
                          "     KOT RESTAURANT PERFORMANCE      \n" +
                          "====================================\n\n" +
                          "Total Orders Successfully Completed: " + totalOrdersCompleted + "\n" +
                          "Total Orders Rejected: " + totalOrdersRejected + "\n" +
                          "Total Revenue: ₹" + String.format("%.2f", totalRevenue) + "\n\n" +
                          "Note: Entries are read-only and immutable for audit purposes.\n";
            performanceArea.setText(text);
        });
    }

    @Override
    public void onOrderCompleted(Order order) {
        totalOrdersCompleted++;
        totalRevenue += order.getTotalAmount();
        updatePerformanceArea();
    }

    @Override
    public void onOrderCancelled(Order order) {
        totalOrdersRejected++;
        updatePerformanceArea();
        SwingUtilities.invokeLater(() -> {
            alertsListModel.addElement("ALERT: Order #" + order.getOrderId() + " rejected by " + order.getChefName() + " (Reason: " + order.getRejectionReason() + ")");
        });
    }
}
