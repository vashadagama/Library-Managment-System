package com.example.lims.model;

import com.example.lims.enums.UserRole;
import com.example.lims.enums.UserStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String libraryCardNumber;
    private String firstName;
    private String lastName;
    private String patronimyc;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private Integer maxActiveLoans = 5;

    private String phoneNumber;
    private LocalDate registrationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    public User() {}

    public User(String firstName, String lastName, String patronimyc, String email,
                UserRole role, String password, String libraryCardNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronimyc = patronimyc;
        this.email = email;
        this.role = role;
        this.libraryCardNumber = libraryCardNumber;
        this.password = password;
    }

    public static User createReader(String firstName, String lastName, String patronimyc,
                                    String email, String libraryCardNumber) {
        User reader = new User();
        reader.firstName = firstName;
        reader.lastName = lastName;
        reader.patronimyc = patronimyc;
        reader.email = email;
        reader.role = UserRole.READER;
        reader.libraryCardNumber = libraryCardNumber;
        reader.password = null;
        return reader;
    }

    public String getFullName() {
        String full = firstName + " " + lastName;
        if (patronimyc != null && !patronimyc.isBlank()) {
            full += " " + patronimyc;
        }
        return full;
    }

    public UUID getId() { return id; }
    public String getLibraryCardNumber() { return libraryCardNumber; }
    public void setLibraryCardNumber(String libraryCardNumber) { this.libraryCardNumber = libraryCardNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPatronimyc() { return patronimyc; }
    public void setPatronimyc(String patronimyc) { this.patronimyc = patronimyc; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Integer getMaxActiveLoans() { return maxActiveLoans; }
    public void setMaxActiveLoans(Integer maxActiveLoans) { this.maxActiveLoans = maxActiveLoans; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}