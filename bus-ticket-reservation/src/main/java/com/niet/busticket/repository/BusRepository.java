package com.niet.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niet.busticket.entity.Bus;

public interface BusRepository extends JpaRepository<Bus, Long> {
}
