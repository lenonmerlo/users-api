package com.lenon.users.service;


import com.lenon.users.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryUserService implements UserService{
    private final Map<Long, UserDTO> db = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public List<UserDTO> findAll() {
        return new ArrayList<>(db.values());
    }
}
