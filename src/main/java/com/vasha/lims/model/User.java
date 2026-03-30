package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.UUID;
import main.java.com.vasha.lims.enums.UserRole;
import main.java.com.vasha.lims.enums.UserStatus;
import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class User {
    private UUID id;
    private String libraryCardNumber;
    private String firstName;
    private String lastName;
    private String patronimyc;
    private String email;
    private String phoneNumber;
    private LocalDate registrationDate;
    private UserRole role;
    private UserStatus status;

    public User(String firstName, String lastName, String patronimic, String email, UserRole role, String libraryCardNumber) {
        setFirstName(firstName);
        setLastName(lastName);
        setPatronimyc(patronimyc);
        setLibraryCardNumber(libraryCardNumber);
        setEmail(email);
        setRole(role);

        this.id = UUID.randomUUID();
        this.registrationDate = LocalDate.now();
        this.status = UserStatus.ACTIVE;
    }

    public User() {}

    

    public String getFullName() {
        return firstName + " " + lastName + " " + patronimyc;
    }

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

    public String getPatronimyc() {
        return patronimyc;
    }

    public final void setPatronimyc(String patronimyc) {
        this.patronimyc = patronimyc;
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        checkNotBlank(email, "Email");
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public final void setPhoneNumber(String phoneNumber) {
        checkNotBlank(phoneNumber, "Номер телефона");
        this.phoneNumber = phoneNumber;
    }

    public String getLibraryCardNumber() {
        return libraryCardNumber;
    }

    public final void setLibraryCardNumber(String libraryCardNumber){
        checkNotBlank(libraryCardNumber, "Номер библиотекчного билета");
        this.libraryCardNumber = libraryCardNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public final void setRole(UserRole role) {
        checkNotNull(role, "Роль");
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
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
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", libraryCardNumber='" + libraryCardNumber + '\'' +
                ", fisrtName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", patronymic='" + patronimyc + '\'' +
                ", role=" + role + '\'' +
                ", email=" + email + '\'' +
                ", phoneNumber=" + phoneNumber + '\'' +
                ", registrationDate=" + registrationDate + '\'' +
                ", status=" + status +
                '}';
}
}
