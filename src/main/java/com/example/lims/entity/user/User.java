package com.example.lims.entity.user;

import com.example.lims.entity.status.Role;
import com.example.lims.entity.status.UserStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "library_card_number", unique = true, length = 50)
    private String libraryCardNumber;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "max_active_loans", nullable = false)
    private Integer maxActiveLoans;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private UserStatus status;


    @PrePersist
    void onCreate(){
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }
}