package com.kot.model;

import java.util.List;

/**
 * Represents a KOT Order pushed by Waiter and pulled by Chef.
 */
public class Order {
    
    public enum Status {
        PENDING, PREPARING, COMPLETED, CANCELLED_REJECTED
    }

    private static int orderIdCounter = 1;

    private int orderId;
    private String waiterName;
    private String chefName;
    private int tableNumber;
    private List<MenuItem> items;
    private double totalAmount;
    private Status status;
    private String rejectionReason;

    public Order(String waiterName, int tableNumber, List<MenuItem> items) {
        this.orderId = orderIdCounter++;
        this.waiterName = waiterName;
        this.tableNumber = tableNumber;
        this.items = items;
        this.status = Status.PENDING;
        this.totalAmount = items.stream().mapToDouble(MenuItem::getPrice).sum();
    }

    public int getOrderId() { return orderId; }
    public String getWaiterName() { return waiterName; }
    public int getTableNumber() { return tableNumber; }
    public List<MenuItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public Status getStatus() { return status; }
    
    public void setStatus(Status status) { this.status = status; }

    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    @Override
    public String toString() {
        StringBuilder itemsStr = new StringBuilder();
        for (MenuItem item : items) {
            itemsStr.append(item.getItemName()).append(", ");
        }
        if (itemsStr.length() > 0) itemsStr.setLength(itemsStr.length() - 2);

        return String.format("Order #%d | Table %d | Waiter: %s | Status: %s | Items: [%s]",
                orderId, tableNumber, waiterName, status, itemsStr.toString());
    }
}
