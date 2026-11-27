package com.niet.busticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.niet.busticket.dto.BusRequest;
import com.niet.busticket.dto.RouteRequest;
import com.niet.busticket.dto.TripSearchResult;
import com.niet.busticket.dto.TripRequest;
import com.niet.busticket.entity.Bus;
import com.niet.busticket.entity.Route;
import com.niet.busticket.entity.Trip;
import com.niet.busticket.service.BusService;
import com.niet.busticket.service.RouteService;
import com.niet.busticket.service.TripService;

import jakarta.validation.Valid;

/**
 * Admin-only endpoints for managing the master data (buses, routes, trips).
 * In a full production build these would sit behind Spring Security with a
 * role check (hasRole("ADMIN")) - omitted here to keep the demo runnable
 * without extra login setup; see the report's Future Work section.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private BusService busService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private TripService tripService;

    /**
     * Example request body:
     * { "busNumber": "UP16AB1234", "operatorName": "NIET Travels", "type": "AC", "totalSeats": 40 }
     */
    @PostMapping("/buses")
    @ResponseStatus(HttpStatus.CREATED)
    public Bus createBus(@Valid @RequestBody BusRequest request) {
        return busService.createBus(request);
    }

    @GetMapping("/buses")
    public List<Bus> getAllBuses() {
        return busService.getAllBuses();
    }

    /**
     * Example request body:
     * { "source": "Delhi", "destination": "Lucknow", "distanceKm": 500 }
     */
    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.CREATED)
    public Route createRoute(@Valid @RequestBody RouteRequest request) {
        return routeService.createRoute(request);
    }

    @GetMapping("/routes")
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    /**
     * Creates a trip AND auto-generates its seat map (see TripService).
     * Example request body:
     * {
     *   "busId": 1,
     *   "routeId": 1,
     *   "departureTime": "2026-10-05T09:00:00",
     *   "arrivalTime": "2026-10-05T17:00:00",
     *   "fare": 850
     * }
     */
    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public Trip createTrip(@Valid @RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }

    /** Returns the trips currently stored in the database for admin verification. */
    @GetMapping("/trips")
    public List<TripSearchResult> getAllTrips() {
        return tripService.getAllTripsForAdmin();
    }
}
