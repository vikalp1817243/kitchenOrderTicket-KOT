package com.kot.exception;

/**
 * Thrown when a Waiter inputs a menu code that does not exist in the database/HashMap.
 */
public class InvalidMenuCodeException extends Exception {
    public InvalidMenuCodeException(String message) {
        super(message);
    }
}
