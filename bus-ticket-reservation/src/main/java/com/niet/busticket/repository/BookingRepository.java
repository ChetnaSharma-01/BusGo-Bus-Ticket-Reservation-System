package com.niet.busticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niet.busticket.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);
}
