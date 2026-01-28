
package com.example.lwms1.exception;

/**
 * Thrown when an entity with a given id/key is not found.
 * Handled globally by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
