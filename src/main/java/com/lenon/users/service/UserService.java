package com.lenon.users.service;

import com.lenon.users.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
}
