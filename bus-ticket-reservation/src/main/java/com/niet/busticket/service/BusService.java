package com.niet.busticket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.niet.busticket.dto.BusRequest;
import com.niet.busticket.entity.Bus;
import com.niet.busticket.entity.BusType;
import com.niet.busticket.exception.ResourceNotFoundException;
import com.niet.busticket.repository.BusRepository;

@Service
public class BusService {

    @Autowired
    private BusRepository busRepository;

    public Bus createBus(BusRequest request) {
        BusType type = BusType.valueOf(request.getType().trim().toUpperCase());
        Bus bus = new Bus(request.getBusNumber(), request.getOperatorName(), type, request.getTotalSeats());
        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public Bus getBusById(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));
    }
}
