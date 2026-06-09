package com.example.lims.model;

import com.example.lims.enums.ItemStatus;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ItemCopyTest {

    @Test
    void testDefaultConstructor() {
        ItemCopy copy = new ItemCopy();
        assertNotNull(copy);
        assertNull(copy.getId());
        assertNull(copy.getItem());
        assertNull(copy.getInventoryNumber());
        assertEquals(ItemStatus.AVAILABLE, copy.getStatus());
    }

    @Test
    void testParameterizedConstructor() {
        LibraryItem item = new Book();
        ItemCopy copy = new ItemCopy(item, "INV-001");

        assertEquals(item, copy.getItem());
        assertEquals("INV-001", copy.getInventoryNumber());
        assertEquals(ItemStatus.AVAILABLE, copy.getStatus());
        assertNull(copy.getId());
    }

    @Test
    void testSettersAndGetters() {
        ItemCopy copy = new ItemCopy();
        LibraryItem item = new Book();

        copy.setItem(item);
        copy.setInventoryNumber("INV-002");
        copy.setStatus(ItemStatus.BORROVED);

        assertNull(copy.getId());
        assertEquals(item, copy.getItem());
        assertEquals("INV-002", copy.getInventoryNumber());
        assertEquals(ItemStatus.BORROVED, copy.getStatus());
    }
}