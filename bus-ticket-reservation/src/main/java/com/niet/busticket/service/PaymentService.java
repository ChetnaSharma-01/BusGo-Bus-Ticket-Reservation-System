package com.niet.busticket.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.niet.busticket.dto.PaymentRequest;
import com.niet.busticket.dto.PaymentResponse;
import com.niet.busticket.entity.Booking;
import com.niet.busticket.entity.BookingStatus;
import com.niet.busticket.entity.Payment;
import com.niet.busticket.entity.PaymentStatus;
import com.niet.busticket.repository.BookingRepository;
import com.niet.busticket.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    /**
     * Simulates a payment gateway call. In place of a real integration
     * (Razorpay/Stripe - see report Future Work), this always succeeds and
     * generates a mock transaction reference, then marks the booking
     * CONFIRMED. Swapping in a real gateway later only requires changing the
     * inside of this method - the controller and DTO contract stay the same.
     */
    @Transactional
    public PaymentResponse processPayment(Long bookingId, PaymentRequest request) {
        Booking booking = bookingService.getBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking is already paid for and confirmed");
        }

        double amount = booking.getTrip().getFare() * booking.getSeats().size();
        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = new Payment(booking, amount, transactionRef);
        payment.setStatus(PaymentStatus.SUCCESS); // simulated - always succeeds
        Payment savedPayment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPayment(savedPayment);
        bookingRepository.save(booking);

        return new PaymentResponse(
                savedPayment.getId(),
                booking.getId(),
                savedPayment.getAmount(),
                savedPayment.getStatus().name(),
                savedPayment.getTransactionRef(),
                savedPayment.getPaymentTime()
        );
    }
}
