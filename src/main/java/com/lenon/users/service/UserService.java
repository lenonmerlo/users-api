package com.lenon.users.service;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
    UserDTO create(UserRequest request);
}
