package main.java.com.vasha.lims.model;

import main.java.com.vasha.lims.enums.UserRole;
import main.java.com.vasha.lims.enums.UserStatus;
import static main.java.com.vasha.lims.util.ValidationUtil.*;
import java.time.LocalDate;
import java.util.UUID;


public class User {
    private UUID id;
    private String libraryCardNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate registrationDate;
    private UserRole role;
    private UserStatus status;


    public User(String fullName, String email, UserRole role, String libraryCardNumber) {
        this.id = UUID.randomUUID();
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.libraryCardNumber = libraryCardNumber;
        this.registrationDate = LocalDate.now();
        this.status = UserStatus.ACTIVE;
    }

    public User() {}


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        checkNotBlank(fullName, "ФИО");
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail() {
        checkNotBlank(email, "Email");
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber() {
        checkNotBlank(phoneNumber, "Номер телефона");
        this.phoneNumber = phoneNumber;
    }

    public String getLibraryCardNumber() {
        return libraryCardNumber;
    }

    public void setLibraryCardNumber(){
        checkNotBlank(libraryCardNumber, "Номер библиотекчного билета");
        this.libraryCardNumber = libraryCardNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole() {
        checkNotNull(role, "Роль");
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus() {
        checkNotNull(status, "Статус");
        this.status = status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public UUID getId() {
        return id;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        User user = (User) object;

        return id != null && id.equals(user.id);

    }

    @Override
    public int hashCose() {
        return id != null ? id.hashCode() : 0;
    }
}
