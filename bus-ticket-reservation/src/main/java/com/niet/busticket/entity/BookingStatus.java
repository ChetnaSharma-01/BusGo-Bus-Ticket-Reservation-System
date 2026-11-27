package com.niet.busticket.entity;

/** Lifecycle states of a booking. */
public enum BookingStatus {
    PENDING,     // created, awaiting payment
    CONFIRMED,   // payment successful
    CANCELLED    // cancelled by user or system
}
