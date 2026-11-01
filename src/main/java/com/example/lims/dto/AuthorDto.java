package com.example.lims.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class AuthorDto {

    private UUID id;

    @NotBlank(message = "Имя автора обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия автора обязательна")
    private String lastName;

    private String middleName;
    private String fullName;

    public AuthorDto() {
    }

    public AuthorDto(UUID id, String firstName, String lastName, String middleName, String fullName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.fullName = fullName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
