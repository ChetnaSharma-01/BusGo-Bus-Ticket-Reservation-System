package com.niet.busticket.repository;

import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.niet.busticket.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByTripId(Long tripId);

    /**
     * Locks the requested seat rows for the duration of the current
     * transaction (PESSIMISTIC_WRITE = "SELECT ... FOR UPDATE"). Any other
     * transaction trying to book the same seats will block until this one
     * commits or rolls back, which is what makes concurrent double-booking
     * impossible. See BookingService.createBooking().
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findSeatsForUpdate(@Param("seatIds") List<Long> seatIds);
}
