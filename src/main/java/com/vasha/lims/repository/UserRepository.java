package main.java.com.vasha.lims.repository;

import main.java.com.vasha.lims.model.Loan;
import main.java.com.vasha.lims.model.User;
import main.java.com.vasha.lims.model.LibraryItem;
import main.java.com.vasha.lims.enums.LoanStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRepository {
    private final List<User> users = new ArrayList<>();

    public final void save(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
        }
    }



}
