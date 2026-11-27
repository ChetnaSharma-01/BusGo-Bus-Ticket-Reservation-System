package com.niet.busticket.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niet.busticket.dto.SeatDto;
import com.niet.busticket.dto.TripSearchResult;
import com.niet.busticket.service.TripService;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    /**
     * Example: GET /api/trips?source=Delhi&destination=Lucknow&date=2026-10-05
     */
    @GetMapping
    public List<TripSearchResult> searchTrips(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tripService.searchTrips(source, destination, date);
    }

    /**
     * Returns the live seat map for a trip - which seats are booked and which are free.
     */
    @GetMapping("/{tripId}/seats")
    public List<SeatDto> getSeats(@PathVariable Long tripId) {
        return tripService.getSeatsForTrip(tripId);
    }
}
