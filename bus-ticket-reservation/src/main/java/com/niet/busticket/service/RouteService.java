package com.niet.busticket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.niet.busticket.dto.RouteRequest;
import com.niet.busticket.entity.Route;
import com.niet.busticket.exception.ResourceNotFoundException;
import com.niet.busticket.repository.RouteRepository;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    public Route createRoute(RouteRequest request) {
        Route route = new Route(request.getSource(), request.getDestination(), request.getDistanceKm());
        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
    }
}
