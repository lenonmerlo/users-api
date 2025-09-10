package com.lenon.users.controller;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;
import com.lenon.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // POST /users -> 201 Created + Location
    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserRequest request) {
        UserDTO saved = service.create(request);
        URI location = URI.create("/users/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }
}
