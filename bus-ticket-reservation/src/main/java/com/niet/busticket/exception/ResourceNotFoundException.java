package com.niet.busticket.exception;

/** Thrown when a requested User, Bus, Route, Trip, Seat, Booking or Payment does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
