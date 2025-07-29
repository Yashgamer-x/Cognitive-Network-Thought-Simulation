package com.yashgamerx.cognitive_thought_network_simulation.exception;
/**
 * An unchecked exception to wrap SQL and data-access errors.
 */
public class DataAccessException extends RuntimeException {

    /**
     * Constructs a new DataAccessException with the specified detail message.
     *
     * @param message the detail message
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new DataAccessException with the specified detail message
     * and root cause.
     *
     * @param message the detail message
     * @param cause   the cause (which is saved for later retrieval)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
