package com.example.lims.service;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;
import com.example.lims.enums.UserStatus;
import com.example.lims.model.User;
import com.example.lims.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    public User createUser(UserCreateDto dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Пользователь с email " + dto.getEmail() + " уже существует!");
        }

        User newUser = new User(
                dto.getFirstName(),
                dto.getLastName(),
                null,
                dto.getEmail(),
                dto.getRole(),
                null,
                "LIB-" + System.currentTimeMillis()
        );

        return userRepository.save(newUser);
    }

    @org.springframework.transaction.annotation.Transactional
    public User changeUserStatus(UUID userId, com.example.lims.enums.UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public User updateMaxLoansLimit(UUID userId, Integer newLimit) {
        if (newLimit < 0) {
            throw new RuntimeException("Лимит не может быть меньше нуля");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setMaxActiveLoans(newLimit);
        return userRepository.save(user);
    }
}