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
    private final List<LibraryItem> works = new ArrayList<>();

    public Author(String firstName, String lastName, LocalDate birthDate) {
        setFirstName(firstName);
        setLastName(lastName);
        setBirthDate(birthDate);

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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public final void setBirthDate(LocalDate birthDate) {
        if (birthDate != null) {
            checkNotInFuture(birthDate, "Дата рождения автора");
        }
        this.birthDate = birthDate;
    }

    public UUID getId() {
        return id;
    }

    public List<LibraryItem> getWorks() {
        return works;
    }

    public void addWork(LibraryItem work) {
        if (work != null && !this.works.contains(work)) {
            this.works.add(work);
            work.addAuthor(this);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Author author = (Author) object;

        return id != null && id.equals(author.id);

    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", firstName=" + firstName +  '\'' +
                ", lastName=" + lastName + '\'' +
                ", birthDate=" + birthDate + '\'' +
                ", worksCount=" + works.size() + '\'' +
                '}';
    }

}
