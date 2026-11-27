package com.niet.busticket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sanity check: if this test passes, all Spring beans (controllers,
 * services, repositories) wired up correctly and the application context
 * started without errors.
 */
@SpringBootTest
class BusTicketReservationApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty - a failure here means a bean could not be created.
    }
}
