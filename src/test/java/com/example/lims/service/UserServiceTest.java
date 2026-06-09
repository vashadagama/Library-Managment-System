package com.example.lims.service;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.enums.UserRole;
import com.example.lims.enums.UserStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.model.User;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createReader() {
        UserCreateDto dto = new UserCreateDto();
        dto.setFirstName("Иван");
        dto.setLastName("Иванов");
        dto.setPatronimyc(null);
        dto.setEmail("test@example.com");
        dto.setRole(UserRole.READER);
        dto.setPassword(null);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals(UserRole.READER, result.getRole());
    }

    @Test
    void changeUserStatus() {
        UUID id = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.changeUserStatus(id, UserStatus.BLOCKED);

        assertEquals(UserStatus.BLOCKED, result.getStatus());
    }
}