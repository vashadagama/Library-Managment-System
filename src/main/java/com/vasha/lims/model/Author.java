package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class Author {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private List<LibraryItem> works;



    public Author(String firstName, String lastName, LocalDate birthDate) {
        setFirstName(firstName);
        setLastName(lastName);


        this.id = UUID.randomUUID();
    }

    public Author() {}



    public String getFirstName() {
        return firstName;
    }

    public final void setFirstName(String firstName) {
        checkNotBlank(firstName, "Имя");
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public final void setLastName(String lastName) {
        checkNotBlank(lastName, "Фамилия");
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

}
