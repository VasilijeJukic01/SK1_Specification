package com.raf.sk.specification.exception;

public class RoomNotFoundException extends RuntimeException {

    /**
     * This exception indicates that a room was not found in the schedule.
     * <p>
     * This exception is thrown when an attempt is made to access a room that is not found in the schedule.
     *
     * @param message - A message describing the reason for the exception.
     */
    public RoomNotFoundException(String message) {
        super(message);
    }

}
