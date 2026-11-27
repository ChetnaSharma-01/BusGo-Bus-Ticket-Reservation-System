package com.niet.busticket.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request body for POST /api/admin/trips */
public class TripRequest {

    @NotNull(message = "busId is required")
    private Long busId;

    @NotNull(message = "routeId is required")
    private Long routeId;

    @NotNull(message = "departureTime is required (ISO format e.g. 2026-10-01T09:00:00)")
    @Future(message = "departureTime must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "arrivalTime is required")
    private LocalDateTime arrivalTime;

    @NotNull(message = "fare is required")
    @Positive(message = "fare must be a positive number")
    private Double fare;

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }
}
