package main.java.com.vasha.lims.service;

import java.util.UUID;
import main.java.com.vasha.lims.enums.UserRole;
import main.java.com.vasha.lims.enums.UserStatus;
import main.java.com.vasha.lims.model.User;
import main.java.com.vasha.lims.repository.UserRepository;

public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }



    public final User registerUser(String firstName, String lastName, String patronimic, String email, UserRole role, String libraryCardNumber) {
        if (userRepo.findByEmail(email) != null) {
            throw new IllegalStateException("Пользователь с таким Email уже существует!");
        }
        if (userRepo.findByLibraryCardNumber(libraryCardNumber) != null) {
            throw new IllegalStateException("Пользователь с таким Номером библиотечной карты уже сущестует!");
        }

        User user = new User(firstName, lastName, patronimic, email, role, libraryCardNumber);
        userRepo.save(user);
        return user;
    }

    public final User findUserById(UUID userId) {
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден!");
        }
        return user;
    }

    public final void blockUser(UUID userId) {
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден!");
        }
        user.setStatus(UserStatus.BLOCKED);
    }
}