package com.niet.busticket.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.niet.busticket.dto.SeatDto;
import com.niet.busticket.dto.TripRequest;
import com.niet.busticket.dto.TripSearchResult;
import com.niet.busticket.entity.Bus;
import com.niet.busticket.entity.Route;
import com.niet.busticket.entity.Seat;
import com.niet.busticket.entity.Trip;
import com.niet.busticket.exception.ResourceNotFoundException;
import com.niet.busticket.repository.SeatRepository;
import com.niet.busticket.repository.TripRepository;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BusService busService;

    @Autowired
    private RouteService routeService;

    /**
     * Creates a Trip for a given Bus + Route, then immediately generates one
     * Seat row per seat on the bus (1..totalSeats) for THIS trip only. This
     * is the design decision described in the report: seats belong to a
     * trip, not to the bus, so the same bus can have an independent seat map
     * on every trip it runs.
     */
    @Transactional
    public Trip createTrip(TripRequest request) {
        Bus bus = busService.getBusById(request.getBusId());
        Route route = routeService.getRouteById(request.getRouteId());

        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }

        Trip trip = new Trip(bus, route, request.getDepartureTime(), request.getArrivalTime(), request.getFare());
        Trip savedTrip = tripRepository.save(trip);

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            seats.add(new Seat(savedTrip, "S" + i));
        }
        seatRepository.saveAll(seats);

        return savedTrip;
    }

    public List<TripSearchResult> searchTrips(String source, String destination, LocalDate date) {
        LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime dayEnd = dayStart.plusDays(1);

        String cleanSource = source == null ? "" : source.trim();
        String cleanDestination = destination == null ? "" : destination.trim();

        List<Trip> trips = tripRepository.searchTrips(cleanSource, cleanDestination, dayStart, dayEnd);
        List<TripSearchResult> results = new ArrayList<>();

        for (Trip trip : trips) {
            long totalSeats = trip.getBus().getTotalSeats();
            long bookedSeats = seatRepository.findByTripId(trip.getId()).stream()
                    .filter(Seat::isBooked)
                    .count();
            long availableSeats = totalSeats - bookedSeats;

            results.add(new TripSearchResult(
                    trip.getId(),
                    trip.getBus().getBusNumber(),
                    trip.getBus().getOperatorName(),
                    trip.getBus().getType().name(),
                    trip.getRoute().getSource(),
                    trip.getRoute().getDestination(),
                    trip.getDepartureTime(),
                    trip.getArrivalTime(),
                    trip.getFare(),
                    availableSeats,
                    totalSeats
            ));
        }
        return results;
    }

    /**
     * Returns all scheduled trips for the admin dashboard.
     * The same DTO used by passenger search is returned so the UI can verify
     * that a trip was actually published with the expected date and route.
     */
    public List<TripSearchResult> getAllTripsForAdmin() {
        List<Trip> trips = tripRepository.findAllForAdmin();
        List<TripSearchResult> results = new ArrayList<>();
        for (Trip trip : trips) {
            long totalSeats = trip.getBus().getTotalSeats();
            long bookedSeats = seatRepository.findByTripId(trip.getId()).stream()
                    .filter(Seat::isBooked)
                    .count();
            results.add(new TripSearchResult(
                    trip.getId(),
                    trip.getBus().getBusNumber(),
                    trip.getBus().getOperatorName(),
                    trip.getBus().getType().name(),
                    trip.getRoute().getSource(),
                    trip.getRoute().getDestination(),
                    trip.getDepartureTime(),
                    trip.getArrivalTime(),
                    trip.getFare(),
                    totalSeats - bookedSeats,
                    totalSeats
            ));
        }
        return results;
    }

    public List<SeatDto> getSeatsForTrip(Long tripId) {
        // Ensure the trip actually exists before returning its seat map
        getTripById(tripId);

        List<Seat> seats = seatRepository.findByTripId(tripId);
        List<SeatDto> seatDtos = new ArrayList<>();
        for (Seat seat : seats) {
            seatDtos.add(new SeatDto(seat.getId(), seat.getSeatNumber(), seat.isBooked()));
        }
        return seatDtos;
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
    }
}
