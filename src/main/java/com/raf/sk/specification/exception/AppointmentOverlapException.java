package com.raf.sk.specification.exception;

public class AppointmentOverlapException extends RuntimeException {

    /**
     * This exception indicates that an appointment is not available or is already booked in the schedule.
     * <p>
     * This exception is thrown when an attempt is made to add an appointment to the schedule, but the specified appointment
     * conflicts with existing appointments.
     *
     * @param message-  A message describing the reason for the exception.
     */
    public AppointmentOverlapException(String message) {
        super(message);
    }
}
