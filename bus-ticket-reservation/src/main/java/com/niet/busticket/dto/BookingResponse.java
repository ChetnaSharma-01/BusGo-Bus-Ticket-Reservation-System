package com.niet.busticket.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Response body returned after creating a booking, and for booking history. */
public class BookingResponse {

    private Long bookingId;
    private Long tripId;
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    private List<String> seatNumbers;
    private String status;
    private Double totalAmount;
    private LocalDateTime bookingTime;

    public BookingResponse() {
    }

    public BookingResponse(Long bookingId, Long tripId, String source, String destination,
                            LocalDateTime departureTime, List<String> seatNumbers, String status,
                            Double totalAmount, LocalDateTime bookingTime) {
        this.bookingId = bookingId;
        this.tripId = tripId;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.seatNumbers = seatNumbers;
        this.status = status;
        this.totalAmount = totalAmount;
        this.bookingTime = bookingTime;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}
