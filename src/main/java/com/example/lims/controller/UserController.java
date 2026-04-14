package com.example.lims.controller;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;
import com.example.lims.enums.UserStatus;
import jakarta.validation.Valid;
import com.example.lims.model.User;
import com.example.lims.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @PatchMapping("/{id}/status")
    public User changeStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        UserStatus newStatus = UserStatus.valueOf(statusStr.toUpperCase());
        return userService.changeUserStatus(id, newStatus);
    }

    @PatchMapping("/{id}/max-loans")
    public User updateMaxLoans(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        Integer newLimit = body.get("maxActiveLoans");
        if (newLimit == null) {
            throw new RuntimeException("Поле maxActiveLoans обязательно");
        }
        return userService.updateMaxLoansLimit(id, newLimit);
    }
}