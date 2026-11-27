package com.niet.busticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.niet.busticket.dto.PaymentRequest;
import com.niet.busticket.dto.PaymentResponse;
import com.niet.busticket.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Example request body:
     * { "paymentMethod": "UPI" }
     *
     * On success, this also flips the booking's status to CONFIRMED.
     */
    @PostMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentResponse processPayment(@PathVariable Long bookingId,
                                           @Valid @RequestBody PaymentRequest request) {
        return paymentService.processPayment(bookingId, request);
    }
}
