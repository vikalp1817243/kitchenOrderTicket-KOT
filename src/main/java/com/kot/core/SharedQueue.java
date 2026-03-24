package com.kot.core;

import com.kot.db.OrderDAO;
import com.kot.event.OrderUpdateListener;
import com.kot.exception.KitchenOverloadException;
import com.kot.model.Order;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Producer-Consumer synchronized queue holding PENDING orders.
 */
public class SharedQueue {
    private final Queue<Order> pendingOrders;
    private final int MAX_CAPACITY;
    private final List<OrderUpdateListener> listeners;

    public SharedQueue(int maxCapacity) {
        this.pendingOrders = new LinkedList<>();
        this.MAX_CAPACITY = maxCapacity;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void addOrderUpdateListener(OrderUpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeOrderUpdateListener(OrderUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Waiter (Producer) pushes an order to the queue.
     */
    public synchronized void addOrder(Order order) throws KitchenOverloadException {
        if (pendingOrders.size() >= MAX_CAPACITY) {
            throw new KitchenOverloadException("Kitchen is overloaded! Cannot accept more than " + MAX_CAPACITY + " orders currently.");
        }
        
        pendingOrders.add(order);
        System.out.println("Waiters pushed order #" + order.getOrderId() + " to queue.");
        
        // Notify any waiting Chefs that an order is now available
        notifyAll();
    }

    /**
     * Chef (Consumer) pulls the next order from the queue.
     */
    public synchronized Order getNextOrder() throws InterruptedException {
        while (pendingOrders.isEmpty()) {
            System.out.println("Kitchen queue is empty. Chef is waiting...");
            wait(); // Wait until a Waiter adds an order
        }
        Order order = pendingOrders.poll();
        order.setStatus(Order.Status.PREPARING);
        return order;
    }

    /**
     * Complete an order.
     */
    public void completeOrder(Order order, String chefName) {
        order.setChefName(chefName);
        order.setStatus(Order.Status.COMPLETED);
        
        // Save to DB
        OrderDAO.saveOrderProcessing(order);
        
        // Notify Listeners (Waiters/Owners)
        for (OrderUpdateListener listener : listeners) {
            listener.onOrderCompleted(order);
        }
    }

    /**
     * Cancel/Reject an order.
     */
    public void cancelOrder(Order order, String chefName, String reason) {
        order.setChefName(chefName);
        order.setRejectionReason(reason);
        order.setStatus(Order.Status.CANCELLED_REJECTED);
        
        // Save to DB
        OrderDAO.saveOrderProcessing(order);
        
        // Notify Listeners (Waiters/Owners)
        for (OrderUpdateListener listener : listeners) {
            listener.onOrderCancelled(order);
        }
    }
    
    /**
     * Get a snapshot list of current pending orders.
     */
    public synchronized List<Order> getPendingOrdersSnapshot() {
        return new LinkedList<>(pendingOrders);
    }
}
