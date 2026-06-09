package com.example.lims.model;

import com.example.lims.enums.UserRole;
import com.example.lims.enums.UserStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNotNull(user);
        assertEquals(5, user.getMaxActiveLoans());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getRegistrationDate());
    }

    @Test
    void testParameterizedConstructor() {
        User user = new User("Иван", "Иванов", "Иванович", "test@example.com", UserRole.READER, "pass123", "CARD-001");
        assertEquals("Иван", user.getFirstName());
        assertEquals("Иванов", user.getLastName());
        assertEquals("Иванович", user.getPatronimyc());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserRole.READER, user.getRole());
        assertEquals("CARD-001", user.getLibraryCardNumber());
        assertEquals("pass123", user.getPassword());
    }

    @Test
    void testCreateReader() {
        User user = User.createReader("Петр", "Петров", "Петрович", "p@example.com", "CARD-002");
        assertEquals("Петр", user.getFirstName());
        assertEquals(UserRole.READER, user.getRole());
        assertNull(user.getPassword());
        assertEquals("CARD-002", user.getLibraryCardNumber());
    }

    @Test
    void testGetFullName() {
        User user = new User();
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setPatronimyc("Иванович");
        assertEquals("Иван Иванов Иванович", user.getFullName());
        user.setPatronimyc(null);
        assertEquals("Иван Иванов", user.getFullName());
        user.setPatronimyc("");
        assertEquals("Иван Иванов", user.getFullName());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        user.setLibraryCardNumber("CARD-999");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPatronimyc("T");
        user.setEmail("test2@example.com");
        user.setPhoneNumber("+7999");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.BLOCKED);
        user.setMaxActiveLoans(10);
        user.setPassword("secret");

        assertNull(user.getId());
        assertEquals("CARD-999", user.getLibraryCardNumber());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("T", user.getPatronimyc());
        assertEquals("test2@example.com", user.getEmail());
        assertEquals("+7999", user.getPhoneNumber());
        assertNotNull(user.getRegistrationDate());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(UserStatus.BLOCKED, user.getStatus());
        assertEquals(10, user.getMaxActiveLoans());
        assertEquals("secret", user.getPassword());
    }
}