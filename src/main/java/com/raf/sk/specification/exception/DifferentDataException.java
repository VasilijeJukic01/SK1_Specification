package com.raf.sk.specification.exception;

public class DifferentDataException extends RuntimeException {

    /**
     * This exception indicates that a room was not found in the schedule.
     * <p>
     * This exception is thrown when an attempt is made to change an appointment with new one and its data is different.
     *
     * @param message - A message describing the reason for the exception.
     */
    public DifferentDataException(String message) {
        super(message);
    }

}
