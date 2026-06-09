package com.example.lims.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

class AuthorTest {

    @Test
    void testDefaultConstructor() {
        Author author = new Author();
        assertNotNull(author);
        assertNull(author.getId());
        assertNull(author.getFirstName());
        assertNull(author.getLastName());
        assertNull(author.getMiddleName());
        assertNotNull(author.getWorks());
        assertTrue(author.getWorks().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        Author author = new Author("Иван", "Иванов", "Иванович");
        assertEquals("Иван", author.getFirstName());
        assertEquals("Иванов", author.getLastName());
        assertEquals("Иванович", author.getMiddleName());
    }

    @Test
    void testSettersAndGetters() {
        Author author = new Author();
        UUID id = UUID.randomUUID();
        author.setId(id);
        author.setFirstName("Петр");
        author.setLastName("Петров");
        author.setMiddleName("Петрович");

        assertEquals(id, author.getId());
        assertEquals("Петр", author.getFirstName());
        assertEquals("Петров", author.getLastName());
        assertEquals("Петрович", author.getMiddleName());
    }

    @Test
    void testAddWork() {
        Author author = new Author("Иван", "Иванов", null);
        LibraryItem item = new Book();
        author.addWork(item);

        assertEquals(1, author.getWorks().size());
        assertTrue(author.getWorks().contains(item));

        // idempotent
        author.addWork(item);
        assertEquals(1, author.getWorks().size());
    }

    @Test
    void testAddWorkNull() {
        Author author = new Author();
        author.addWork(null);
        assertTrue(author.getWorks().isEmpty());
    }
}
