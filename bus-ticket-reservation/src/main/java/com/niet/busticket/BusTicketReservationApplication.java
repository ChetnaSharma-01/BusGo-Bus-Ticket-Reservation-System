package com.niet.busticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Bus Ticket Reservation System.
 *
 * Run this class as a Java Application (or "Spring Boot App") from Eclipse
 * once the project has been imported as a Maven project.
 */
@SpringBootApplication
public class BusTicketReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTicketReservationApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" Bus Ticket Reservation System started successfully");
        System.out.println(" API base URL: http://localhost:8080");
        System.out.println("=================================================");
    }
}
