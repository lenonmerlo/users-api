package com.lenon.users.service;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;
import com.lenon.users.exception.ConflictException;
import com.lenon.users.exception.DomainValidationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryUserService implements UserService {

    private final Map<Long, UserDTO> db = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "USER", "MANAGER"); // regra simples de negócio

    @Override
    public List<UserDTO> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    public UserDTO create(UserRequest request) {
        // Regra de negócio: role permitida
        if (!ALLOWED_ROLES.contains(request.role().toUpperCase())) {
            throw new DomainValidationException("Role must be one of: " + ALLOWED_ROLES);
        }
        // Conflito: e-mail único (case-insensitive)
        boolean exists = db.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(request.email()));
        if (exists) throw new ConflictException("Email already in use");

        long id = seq.getAndIncrement();
        UserDTO saved = new UserDTO(id, request.name(), request.email(), request.role().toUpperCase());
        db.put(id, saved);
        return saved;
    }
}
