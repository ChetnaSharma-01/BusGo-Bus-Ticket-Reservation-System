package com.niet.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niet.busticket.entity.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
