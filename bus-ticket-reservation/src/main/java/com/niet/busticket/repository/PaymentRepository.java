package com.niet.busticket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niet.busticket.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
}
