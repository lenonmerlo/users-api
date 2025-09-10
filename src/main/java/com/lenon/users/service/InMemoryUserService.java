package com.lenon.users.service;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;
import com.lenon.users.exception.ConflictException;
import com.lenon.users.exception.DomainValidationException;
import com.lenon.users.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryUserService implements UserService {

    private final Map<Long, UserDTO> db = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "USER", "MANAGER");

    // helper para limpar entradas
    private record Clean(String name, String email, String role) {}
    private Clean sanitize(UserRequest r) {
        String name  = r.name()  == null ? null : r.name().trim();
        String email = r.email() == null ? null : r.email().trim();
        String role  = r.role()  == null ? null : r.role().trim().toUpperCase();
        return new Clean(name, email, role);
    }

    @Override
    public List<UserDTO> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    public UserDTO create(UserRequest request) {
        Clean c = sanitize(request);
        validateBusiness(c.name, c.email, c.role);
        ensureEmailUnique(c.email, null);

        long id = seq.getAndIncrement();
        UserDTO saved = new UserDTO(id, c.name, c.email, c.role);
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
        if (existing == null) throw new NotFoundException("User " + id + " not found");

        Clean c = sanitize(request);
        validateBusiness(c.name, c.email, c.role);
        ensureEmailUnique(c.email, id);

        UserDTO updated = new UserDTO(id, c.name, c.email, c.role);
        db.put(id, updated);
        return updated;
    }

    @Override
    public void delete(Long id) {
        UserDTO removed = db.remove(id);
        if (removed == null) {
            throw new NotFoundException("User " +id + " not found");
        }
    }

    // ---------- validações ----------
    private void validateBusiness(String name, String email, String role) {
        if (name == null || name.isBlank())
            throw new DomainValidationException("Name cannot be blank");

        if (email == null || !email.contains("@"))
            throw new DomainValidationException("Invalid email");

        if (role == null || role.isBlank() || !ALLOWED_ROLES.contains(role))
            throw new DomainValidationException("Role must be one of: " + ALLOWED_ROLES);
    }

    private void ensureEmailUnique(String email, Long ignoreId) {
        boolean exists = db.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(email) && !Objects.equals(u.id(), ignoreId));
        if (exists) throw new ConflictException("Email already in use");
    }
}
