package com.example.lims.service;

import com.example.lims.dto.ItemCopyCreateDto;
import com.example.lims.dto.ItemCopyDto;
import com.example.lims.entity.item.Book;
import com.example.lims.entity.item.ItemCopy;
import com.example.lims.entity.status.ItemStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.ItemStatusRepository;
import com.example.lims.repository.LibraryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCopyServiceTest {

    @Mock private ItemCopyRepository itemCopyRepository;
    @Mock private LibraryItemRepository libraryItemRepository;
    @Mock private ItemStatusRepository itemStatusRepository;
    @Mock private AuditService auditService;

    @InjectMocks private ItemCopyService itemCopyService;

    private static ItemStatus status(String name) {
        ItemStatus status = new ItemStatus();
        status.setId(UUID.randomUUID());
        status.setName(name);
        return status;
    }

    @Test
    void createCopyStartsWithAvailableStatus() {
        UUID itemId = UUID.randomUUID();
        Book book = new Book();
        book.setId(itemId);
        book.setTitle("Книга");

        ItemCopyCreateDto dto = new ItemCopyCreateDto();
        dto.setLibraryItemId(itemId);
        dto.setInventoryNumber("INV-001");

        when(libraryItemRepository.findById(itemId)).thenReturn(Optional.of(book));
        when(itemCopyRepository.existsByInventoryNumber("INV-001")).thenReturn(false);
        when(itemStatusRepository.findByNameIgnoreCase("AVAILABLE")).thenReturn(Optional.of(status("AVAILABLE")));
        when(itemCopyRepository.save(any(ItemCopy.class))).thenAnswer(invocation -> {
            ItemCopy copy = invocation.getArgument(0);
            copy.setId(UUID.randomUUID());
            return copy;
        });

        ItemCopyDto result = itemCopyService.createCopy(dto);

        assertEquals("INV-001", result.getInventoryNumber());
        assertEquals("AVAILABLE", result.getStatusName());
        assertEquals(itemId, result.getLibraryItemId());
    }

    @Test
    void updateStatusRejectsManualAvailableForBorrowedCopy() {
        UUID copyId = UUID.randomUUID();
        ItemCopy copy = new ItemCopy();
        copy.setId(copyId);
        copy.setStatus(status("BORROWED"));

        when(itemCopyRepository.findById(copyId)).thenReturn(Optional.of(copy));
        when(itemStatusRepository.findByNameIgnoreCase("AVAILABLE")).thenReturn(Optional.of(status("AVAILABLE")));

        assertThrows(BusinessLogicException.class,
                () -> itemCopyService.updateStatus(copyId, "AVAILABLE"));
    }

    @Test
    void deleteBorrowedCopyIsForbidden() {
        UUID copyId = UUID.randomUUID();
        ItemCopy copy = new ItemCopy();
        copy.setId(copyId);
        copy.setStatus(status("BORROWED"));

        when(itemCopyRepository.findById(copyId)).thenReturn(Optional.of(copy));

        assertThrows(BusinessLogicException.class, () -> itemCopyService.deleteCopy(copyId));
    }
}
