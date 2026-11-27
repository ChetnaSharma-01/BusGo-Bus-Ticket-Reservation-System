package com.niet.busticket.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.niet.busticket.dto.BookingRequest;
import com.niet.busticket.dto.BookingResponse;
import com.niet.busticket.entity.Booking;
import com.niet.busticket.entity.BookingStatus;
import com.niet.busticket.entity.Seat;
import com.niet.busticket.entity.Trip;
import com.niet.busticket.entity.User;
import com.niet.busticket.exception.ResourceNotFoundException;
import com.niet.busticket.exception.SeatUnavailableException;
import com.niet.busticket.repository.BookingRepository;
import com.niet.busticket.repository.SeatRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    /**
     * Creates a booking for one or more seats on a trip.
     *
     * Concurrency safety: findSeatsForUpdate() takes a PESSIMISTIC_WRITE lock
     * (a "SELECT ... FOR UPDATE") on the requested seat rows for the life of
     * this transaction. If two requests try to book the same seat at the
     * same time, the database itself serialises them - the second request's
     * findSeatsForUpdate() call simply waits until the first transaction
     * commits (releasing the lock), at which point isBooked will already be
     * true and the second request fails cleanly with SeatUnavailableException
     * instead of allowing a double booking.
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        User user = userService.getUserById(request.getUserId());
        Trip trip = tripService.getTripById(request.getTripId());

        List<Seat> seats = seatRepository.findSeatsForUpdate(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more selected seats do not exist");
        }

        for (Seat seat : seats) {
            if (!seat.getTrip().getId().equals(trip.getId())) {
                throw new IllegalArgumentException("Seat " + seat.getSeatNumber() + " does not belong to this trip");
            }
            if (seat.isBooked()) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " is already booked");
            }
        }

        Booking booking = new Booking(user, trip);
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            seat.setBooked(true);
            seat.setBooking(savedBooking);
        }
        seatRepository.saveAll(seats);

        double totalAmount = trip.getFare() * seats.size();

        return toResponse(savedBooking, seats, totalAmount);
    }

    public List<BookingResponse> getBookingsForUser(Long userId) {
        // Validate the user exists so callers get a clean 404 rather than an empty list for a typo'd id
        userService.getUserById(userId);

        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(userId);
        List<BookingResponse> responses = new ArrayList<>();
        for (Booking booking : bookings) {
            double amount = booking.getTrip().getFare() * booking.getSeats().size();
            responses.add(toResponse(booking, booking.getSeats(), amount));
        }
        return responses;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release the seats back to the available pool
        List<Seat> seats = booking.getSeats();
        for (Seat seat : seats) {
            seat.setBooked(false);
            seat.setBooking(null);
        }
        seatRepository.saveAll(seats);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    private BookingResponse toResponse(Booking booking, List<Seat> seats, double totalAmount) {
        List<String> seatNumbers = seats.stream().map(Seat::getSeatNumber).collect(Collectors.toList());
        return new BookingResponse(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getTrip().getRoute().getSource(),
                booking.getTrip().getRoute().getDestination(),
                booking.getTrip().getDepartureTime(),
                seatNumbers,
                booking.getStatus().name(),
                totalAmount,
                booking.getBookingTime()
        );
    }
}
