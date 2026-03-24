package com.kot.exception;

/**
 * Thrown when the SharedQueue reaches its maximum capacity.
 */
public class KitchenOverloadException extends Exception {
    public KitchenOverloadException(String message) {
        super(message);
    }
}
