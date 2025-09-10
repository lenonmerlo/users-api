package com.lenon.users.service;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;
import com.lenon.users.exception.ConflictException;
import com.lenon.users.exception.DomainValidationException;
import com.lenon.users.exception.NotFoundException;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public  class InMemoryUserService implements UserService {

    private final Map<Long, UserDTO> db = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "USER", "MANAGER");

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

    @Override
    public UserDTO findById(Long id) {
        UserDTO u = db.get(id);
        if (u == null) throw new NotFoundException("User " + id + " not found");
        return u;
    }

    @Override
    public UserDTO update(Long id, UserRequest request) {
        UserDTO existing = db.get(id);
        if (existing == null) throw new NotFoundException("User " + " not found");

        validateBusiness(request);
        ensureEmailUnique(request.email(), id);

        UserDTO updated = new UserDTO(id, request.name(), request.email(), request.role().toUpperCase());
        db.put(id, updated);
        return updated;
    }

    private void validateBusiness(UserRequest r) {
        if (r.name() == null || r.name().isBlank())
            throw new DomainValidationException("Name cannot be blank");
        if (r.email() == null || !r.email().contains("@"))
            throw new DomainValidationException("Invalid email");
        if (r.role() == null || !r.role().isBlank() || ALLOWED_ROLES.contains(r.role().toUpperCase()))
            throw new DomainValidationException("Role must be one of: " + ALLOWED_ROLES);
    }

    private void ensureEmailUnique(String email, Long ignoreId) {
        boolean exists = db.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(email) && !Objects.equals(u.id(), ignoreId));
        if (exists) throw new ConflictException("Email already in use");
    }


}
