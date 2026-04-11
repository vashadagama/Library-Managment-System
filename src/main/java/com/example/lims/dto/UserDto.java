package com.example.lims.dto;

import com.example.lims.enums.UserRole;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String fullName;
    private String email;
    private UserRole role;

    public UserDto(UUID id, String fullName, String email, UserRole role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

}