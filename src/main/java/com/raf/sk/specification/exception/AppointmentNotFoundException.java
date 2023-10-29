package com.raf.sk.specification.exception;

public class AppointmentNotFoundException extends RuntimeException {

    /**
     * This exception indicates that an appointment was not found in the schedule.
     * <p>
     * This exception is thrown when an attempt is made to access an appointment that is not found in the schedule.
     *
     * @param message - A message describing the reason for the exception.
     */
    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
