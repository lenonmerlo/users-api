package com.lenon.users.controller;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;
import com.lenon.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;
    public UserController(UserService service) {
        this.service = service;
    }

    // GET /users -> 200 OK
    @GetMapping
    public List<UserDTO> list() {
        return service.findAll();
    }

    // GET /users/{id} -> 200 ou 404
    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    // POST /users -> 201 Created + Location
    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserRequest request) {
        UserDTO saved = service.create(request);
        URI location = URI.create("/users/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /users/{id} -> 200 OK
    @PutMapping("/{id}")
    public UserDTO update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }

    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
