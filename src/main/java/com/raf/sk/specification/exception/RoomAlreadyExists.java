package com.raf.sk.specification.exception;

public class RoomAlreadyExists extends RuntimeException {

    /**
     * This exception indicates that a room already exists.
     *
     * <p>
     * This exception is thrown when an attempt is made to add new room and that room already exists.
     *
     * @param message - A message describing the reason for the exception.
     */
    public RoomAlreadyExists(String message) {
        super(message);
    }

}
