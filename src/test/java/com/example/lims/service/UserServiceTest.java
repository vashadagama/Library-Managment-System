package com.example.lims.service;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;
import com.example.lims.entity.status.Role;
import com.example.lims.entity.status.UserStatus;
import com.example.lims.entity.user.User;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.RoleRepository;
import com.example.lims.repository.UserRepository;
import com.example.lims.repository.UserStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserStatusRepository userStatusRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SystemParameterService systemParameterService;

    @InjectMocks private UserService userService;

    private static Role role(String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return role;
    }

    private static UserStatus status(String name) {
        UserStatus status = new UserStatus();
        status.setId(UUID.randomUUID());
        status.setName(name);
        return status;
    }

    private static UserCreateDto readerDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setFirstName("Иван");
        dto.setLastName("Иванов");
        dto.setEmail("reader@example.com");
        dto.setRole("READER");
        return dto;
    }

    @Test
    void createReaderWithoutPassword() {
        UserCreateDto dto = readerDto();
        AtomicReference<User> saved = new AtomicReference<>();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("READER")).thenReturn(Optional.of(role("READER")));
        when(userStatusRepository.findByNameIgnoreCase("ACTIVE")).thenReturn(Optional.of(status("ACTIVE")));
        when(systemParameterService.getInt("max_active_loans_default", 5)).thenReturn(5);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            saved.set(user);
            return user;
        });

        UserDto result = userService.createUser(dto);

        assertEquals("READER", result.getRoleName());
        assertEquals("ACTIVE", result.getStatusName());
        assertEquals(5, saved.get().getMaxActiveLoans());
        assertNull(saved.get().getPasswordHash());
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        UserCreateDto dto = readerDto();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(BusinessLogicException.class, () -> userService.createUser(dto));
    }

    @Test
    void createLibrarianRequiresPassword() {
        UserCreateDto dto = readerDto();
        dto.setRole("LIBRARIAN");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("LIBRARIAN")).thenReturn(Optional.of(role("LIBRARIAN")));
        when(userStatusRepository.findByNameIgnoreCase("ACTIVE")).thenReturn(Optional.of(status("ACTIVE")));

        assertThrows(BusinessLogicException.class, () -> userService.createUser(dto));
    }

    @Test
    void changeUserStatusUpdatesStatus() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setRole(role("READER"));
        user.setStatus(status("ACTIVE"));
        UserStatus blocked = status("BLOCKED");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userStatusRepository.findByNameIgnoreCase("BLOCKED")).thenReturn(Optional.of(blocked));
        when(userRepository.save(user)).thenReturn(user);
        when(loanRepository.countByUserIdAndStatusNames(eq(user.getId()), any())).thenReturn(0L);

        UserDto result = userService.changeUserStatus(user.getId(), "BLOCKED");

        assertEquals("BLOCKED", result.getStatusName());
    }
}
