package com.kot.event;

import com.kot.model.Order;

/**
 * Interface to notify listeners when an order is completed or cancelled.
 */
public interface OrderUpdateListener {
    void onOrderCompleted(Order order);
    void onOrderCancelled(Order order);
}
