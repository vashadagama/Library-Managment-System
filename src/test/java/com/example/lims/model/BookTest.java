package com.example.lims.model;

import com.example.lims.enums.BookGenre;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testDefaultConstructor() {
        Book book = new Book();
        assertNotNull(book);
        assertNull(book.getGenre());
        assertNull(book.getPageCount());
        assertNull(book.getIsbn());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate date = LocalDate.now();
        Book book = new Book("Title", "Pub", date, "123-456", "Loc", "RU", BookGenre.FICTION, 300);

        assertEquals("Title", book.getTitle());
        assertEquals("Pub", book.getPublisher());
        assertEquals(date, book.getPublicationDate());
        assertEquals("Loc", book.getLocation());
        assertEquals("RU", book.getLanguage());
        assertEquals(BookGenre.FICTION, book.getGenre());
        assertEquals(300, book.getPageCount());
        assertEquals("123-456", book.getIsbn());
        assertEquals("Книга", book.getItemType());
    }

    @Test
    void testSettersAndGetters() {
        Book book = new Book();
        book.setGenre(BookGenre.SCIENCE);
        book.setPageCount(450);
        book.setIsbn("987-654");

        assertEquals(BookGenre.SCIENCE, book.getGenre());
        assertEquals(450, book.getPageCount());
        assertEquals("987-654", book.getIsbn());
    }
}