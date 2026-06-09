package com.example.lims.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LibraryItemTest {

    static class TestLibraryItem extends LibraryItem {
        public TestLibraryItem() {}
        public TestLibraryItem(String title, String publisher, LocalDate pubDate, String loc, String lang) {
            super(title, publisher, pubDate, loc, lang);
        }
        @Override
        public String getItemType() { return "Test"; }
    }

    @Test
    void testDefaultConstructor() {
        TestLibraryItem item = new TestLibraryItem();
        assertNotNull(item);
        assertNotNull(item.getAuthors());
        assertTrue(item.getAuthors().isEmpty());
        assertNotNull(item.getAddedToLibraryDate());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate date = LocalDate.now();
        TestLibraryItem item = new TestLibraryItem("Title", "Pub", date, "Shelf1", "RU");
        assertEquals("Title", item.getTitle());
        assertEquals("Pub", item.getPublisher());
        assertEquals(date, item.getPublicationDate());
        assertEquals("Shelf1", item.getLocation());
        assertEquals("RU", item.getLanguage());
    }

    @Test
    void testSettersAndGetters() {
        TestLibraryItem item = new TestLibraryItem();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        item.setTitle("New Title");
        item.setLocation("New Loc");
        item.setLanguage("EN");
        item.setPublisher("New Pub");
        item.setPublicationDate(date);
        item.setAddedToLibraryDate(date.minusDays(1));

        assertNull(item.getId());
        assertEquals("New Title", item.getTitle());
        assertEquals("New Loc", item.getLocation());
        assertEquals("EN", item.getLanguage());
        assertEquals("New Pub", item.getPublisher());
        assertEquals(date, item.getPublicationDate());
        assertEquals(date.minusDays(1), item.getAddedToLibraryDate());
    }

    @Test
    void testAddAuthor() {
        TestLibraryItem item = new TestLibraryItem();
        Author author = new Author("Иван", "Иванов", null);
        item.addAuthor(author);
        assertEquals(1, item.getAuthors().size());
        assertTrue(item.getAuthors().contains(author));
        assertTrue(author.getWorks().contains(item));
        item.addAuthor(author);
        assertEquals(1, item.getAuthors().size());
    }

    @Test
    void testAddAuthorNull() {
        TestLibraryItem item = new TestLibraryItem();
        item.addAuthor(null);
        assertTrue(item.getAuthors().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        TestLibraryItem item1 = new TestLibraryItem();
        TestLibraryItem item2 = new TestLibraryItem();
        UUID id = UUID.randomUUID();

        assertNotEquals(item1, item2);
        assertNotEquals(item1, new TestLibraryItem());
    }
}