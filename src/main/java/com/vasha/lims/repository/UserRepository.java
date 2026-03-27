package main.java.com.vasha.lims.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import main.java.com.vasha.lims.model.User;
import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class UserRepository {
    private final List<User> users = new ArrayList<>();

    public final void save(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
        }
    }

    public final User findById(UUID id) {
        checkNotNull(id, "Id пользователя");
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public final User findByLibraryCardNumber(String LibraryCardNumber) {
        checkNotNull(LibraryCardNumber, "Номер библиотечного билета пользователя");
        for (User user : users) {
            if (user.getLibraryCardNumber().equals(LibraryCardNumber)) {
                return user;
            }
        }
        return null;
    }

    public final User findByEmail(String email) {
        checkNotBlank(email, "Элекстронная почта пользователя");
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
    
}
