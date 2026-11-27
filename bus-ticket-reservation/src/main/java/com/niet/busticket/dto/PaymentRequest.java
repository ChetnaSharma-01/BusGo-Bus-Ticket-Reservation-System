package com.niet.busticket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/payments/{bookingId}.
 * In this academic version payment is simulated: no real payment gateway is
 * called, but the same shape (method + a mock card/UPI reference) is kept so
 * a real gateway (Razorpay/Stripe) can be dropped in later without changing
 * the API contract - see "Future Work" in the report.
 */
public class PaymentRequest {

    @NotBlank(message = "paymentMethod is required (e.g. CARD, UPI, NETBANKING)")
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
