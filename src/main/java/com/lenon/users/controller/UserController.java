package com.lenon.users.controller;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
