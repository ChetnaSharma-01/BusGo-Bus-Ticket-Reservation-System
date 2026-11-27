package com.niet.busticket.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Request body for POST /api/bookings */
public class BookingRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "tripId is required")
    private Long tripId;

    @NotEmpty(message = "At least one seatId must be selected")
    private List<Long> seatIds;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}
