package com.niet.busticket.dto;

/** One seat in the GET /api/trips/{id}/seats response. */
public class SeatDto {

    private Long seatId;
    private String seatNumber;
    private boolean booked;

    public SeatDto() {
    }

    public SeatDto(Long seatId, String seatNumber, boolean booked) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.booked = booked;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
