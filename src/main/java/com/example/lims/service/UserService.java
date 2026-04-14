package com.example.lims.service;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;
import com.example.lims.enums.UserStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.User;
import com.example.lims.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public User createUser(UserCreateDto dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new BusinessLogicException("Пользователь с email " + dto.getEmail() + " уже существует!");
        }

        User newUser = new User(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getPatronimyc(),
                dto.getEmail(),
                dto.getRole() != null ? dto.getRole() : com.example.lims.enums.UserRole.READER,
                passwordEncoder.encode(dto.getPassword()),
                "LIB-" + System.currentTimeMillis()
        );

        return userRepository.save(newUser);
    }

    @Transactional
    public User changeUserStatus(UUID userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Transactional
    public User updateMaxLoansLimit(UUID userId, Integer newLimit) {
        if (newLimit < 0) {
            throw new BusinessLogicException("Лимит не может быть меньше нуля");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        user.setMaxActiveLoans(newLimit);
        return userRepository.save(user);
    }
}