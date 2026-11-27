package com.niet.busticket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.niet.busticket.dto.RegisterRequest;
import com.niet.busticket.entity.Role;
import com.niet.busticket.entity.User;
import com.niet.busticket.exception.ResourceNotFoundException;
import com.niet.busticket.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        // NOTE: for a production system, hash the password (e.g. with BCryptPasswordEncoder)
        // before saving. Hashing is intentionally left out here to keep the demo simple -
        // see the "Security" note in the report's Non-Functional Requirements section.
        User user = new User(request.getName(), request.getEmail(), passwordEncoder.encode(request.getPassword()), Role.CUSTOMER);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
