package com.example.lims.model;

import com.example.lims.enums.MagazineGenre;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class MagazineTest {

    @Test
    void testDefaultConstructor() {
        Magazine mag = new Magazine();
        assertNotNull(mag);
        assertNull(mag.getGenre());
        assertNull(mag.getPageCount());
        assertFalse(mag.isHasGlossyCover());
        assertNull(mag.getIssn());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate date = LocalDate.now();
        Magazine mag = new Magazine("Mag Title", "Pub", date, "ISSN-123", "Loc", "RU", MagazineGenre.MALE, 50, true);
        assertEquals("Mag Title", mag.getTitle());
        assertEquals("Журнал", mag.getItemType());
        assertEquals(MagazineGenre.MALE, mag.getGenre());
        assertEquals(50, mag.getPageCount());
        assertTrue(mag.isHasGlossyCover());
        assertEquals("ISSN-123", mag.getIssn());
    }

    @Test
    void testSettersAndGetters() {
        Magazine mag = new Magazine();
        mag.setGenre(MagazineGenre.MALE);
        mag.setPageCount(120);
        mag.setHasGlossyCover(true);
        mag.setIssn("ISSN-456");
        assertEquals(MagazineGenre.MALE, mag.getGenre());
        assertEquals(120, mag.getPageCount());
        assertTrue(mag.isHasGlossyCover());
        assertEquals("ISSN-456", mag.getIssn());
    }
}