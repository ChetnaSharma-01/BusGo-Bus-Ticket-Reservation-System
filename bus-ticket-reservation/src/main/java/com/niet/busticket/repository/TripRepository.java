package com.niet.busticket.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.niet.busticket.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t JOIN FETCH t.bus JOIN FETCH t.route "
         + "WHERE LOWER(TRIM(t.route.source)) = LOWER(TRIM(:source)) "
         + "AND LOWER(TRIM(t.route.destination)) = LOWER(TRIM(:destination)) "
         + "AND t.departureTime >= :dayStart AND t.departureTime < :dayEnd "
         + "ORDER BY t.departureTime ASC")
    List<Trip> searchTrips(@Param("source") String source,
                            @Param("destination") String destination,
                            @Param("dayStart") LocalDateTime dayStart,
                            @Param("dayEnd") LocalDateTime dayEnd);

    /** Admin inventory query: fetch bus and route in the same SQL query so the
     * dashboard never depends on lazy-loading after the repository call. */
    @Query("SELECT t FROM Trip t JOIN FETCH t.bus JOIN FETCH t.route ORDER BY t.departureTime ASC")
    List<Trip> findAllForAdmin();
}
