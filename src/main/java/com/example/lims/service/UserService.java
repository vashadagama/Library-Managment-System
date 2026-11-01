package com.example.lims.service;

import com.example.lims.dto.UserCreateDto;
import com.example.lims.dto.UserDto;
import com.example.lims.entity.status.Role;
import com.example.lims.entity.status.UserStatus;
import com.example.lims.entity.user.User;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.RoleRepository;
import com.example.lims.repository.UserRepository;
import com.example.lims.repository.UserStatusRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String ROLE_READER = "READER";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final List<String> ACTIVE_LOAN_STATUSES = List.of("ACTIVE", "RENEWED", "OVERDUE");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemParameterService systemParameterService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserStatusRepository userStatusRepository,
                       LoanRepository loanRepository,
                       PasswordEncoder passwordEncoder,
                       SystemParameterService systemParameterService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userStatusRepository = userStatusRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
        this.systemParameterService = systemParameterService;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<UUID, Long> activeLoansMap = getActiveLoansCountMap();
        return userRepository.findAll(pageable)
                .map(user -> toDto(user, activeLoansMap.getOrDefault(user.getId(), 0L).intValue()));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsersList() {
        Map<UUID, Long> activeLoansMap = getActiveLoansCountMap();
        return userRepository.findAll().stream()
                .map(user -> toDto(user, activeLoansMap.getOrDefault(user.getId(), 0L).intValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(UserCreateDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessLogicException("Пользователь с email " + dto.getEmail() + " уже существует!");
        }

        String roleName = dto.getRole() != null && !dto.getRole().isBlank()
                ? dto.getRole().toUpperCase()
                : ROLE_READER;
        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Роль не найдена: " + roleName));
        UserStatus status = userStatusRepository.findByNameIgnoreCase(STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Статус пользователя ACTIVE не найден"));

        boolean isReader = ROLE_READER.equalsIgnoreCase(role.getName());
        if (!isReader && (dto.getPassword() == null || dto.getPassword().isBlank())) {
            throw new BusinessLogicException("Пароль обязателен для роли " + role.getName());
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMiddleName(dto.getMiddleName());
        user.setEmail(dto.getEmail());
        user.setLibraryCardNumber(generateLibraryCardNumber());
        user.setRole(role);
        user.setStatus(status);
        user.setMaxActiveLoans(systemParameterService.getInt("max_active_loans_default", 5));
        user.setPasswordHash(isReader ? null : passwordEncoder.encode(dto.getPassword()));

        return toDto(userRepository.save(user), 0);
    }

    @Transactional
    public UserDto changeUserStatus(UUID userId, String statusName) {
        User user = getUser(userId);
        UserStatus newStatus = userStatusRepository.findByNameIgnoreCase(statusName)
                .orElseThrow(() -> new ResourceNotFoundException("Статус пользователя не найден: " + statusName));
        user.setStatus(newStatus);
        return toDto(userRepository.save(user), activeLoansCount(userId));
    }

    @Transactional
    public UserDto updateMaxLoansLimit(UUID userId, Integer newLimit) {
        if (newLimit == null || newLimit < 0) {
            throw new BusinessLogicException("Лимит не может быть меньше нуля");
        }
        User user = getUser(userId);
        user.setMaxActiveLoans(newLimit);
        return toDto(userRepository.save(user), activeLoansCount(userId));
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String search) {
        Map<UUID, Long> activeLoansMap = getActiveLoansCountMap();
        return userRepository.searchUsers(search).stream()
                .map(user -> toDto(user, activeLoansMap.getOrDefault(user.getId(), 0L).intValue()))
                .collect(Collectors.toList());
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    private int activeLoansCount(UUID userId) {
        return (int) loanRepository.countByUserIdAndStatusNames(userId, ACTIVE_LOAN_STATUSES);
    }

    private Map<UUID, Long> getActiveLoansCountMap() {
        return loanRepository.countActiveLoansByUser(ACTIVE_LOAN_STATUSES).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private UserDto toDto(User user, int activeLoansCount) {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : null,
                user.getStatus() != null ? user.getStatus().getName() : null,
                user.getLibraryCardNumber(),
                user.getMaxActiveLoans(),
                activeLoansCount
        );
    }

    private String generateLibraryCardNumber() {
        return "LIB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
