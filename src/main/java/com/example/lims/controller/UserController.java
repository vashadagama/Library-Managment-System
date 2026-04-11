package com.example.lims.controller;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;

import jakarta.validation.Valid;
import com.example.lims.model.User;
import com.example.lims.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getAllUsers();
    }
    
    @PostMapping
    public User createUser(@Valid @RequestBody UserCreateDto dto) {
        return userService.createUser(dto); 
    }
}