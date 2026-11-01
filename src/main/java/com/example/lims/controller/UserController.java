package com.example.lims.controller;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;
import com.example.lims.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    public Page<UserDto> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getAllUsers(page, size);
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserCreateDto dto) {
        return userService.createUser(dto);
    }

    @PatchMapping("/{id}/status")
    public UserDto changeStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return userService.changeUserStatus(id, status);
    }

    @PatchMapping("/{id}/max-loans")
    public UserDto updateMaxLoans(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        Integer newLimit = body.get("maxActiveLoans");
        if (newLimit == null) {
            throw new IllegalArgumentException("Поле maxActiveLoans обязательно");
        }
        return userService.updateMaxLoansLimit(id, newLimit);
    }

    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam String q) {
        return userService.searchUsers(q);
    }
}
