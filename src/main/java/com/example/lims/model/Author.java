package com.example.lims.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Имя автора обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия автора обязательна")
    private String lastName;

    private String middleName;

    @JsonIgnore
    @ManyToMany(mappedBy = "authors")
    private List<LibraryItem> works = new ArrayList<>();

    public Author() {
    }

    public Author(String firstName, String lastName, String middleName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
    }

    public void addWork(LibraryItem item) {
        if (item != null && !works.contains(item)) {
            works.add(item);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public List<LibraryItem> getWorks() { return works; }
}