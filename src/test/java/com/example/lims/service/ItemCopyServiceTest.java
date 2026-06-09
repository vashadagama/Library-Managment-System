package com.example.lims.service;

import com.example.lims.dto.ItemCopyCreateDto;
import com.example.lims.enums.ItemStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Book;
import com.example.lims.model.ItemCopy;
import com.example.lims.model.LibraryItem;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.LibraryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCopyServiceTest {

    @Mock
    private ItemCopyRepository itemCopyRepository;
    @Mock
    private LibraryItemRepository libraryItemRepository;

    @InjectMocks
    private ItemCopyService itemCopyService;

    @Test
    void createCopy() {
        UUID itemId = UUID.randomUUID();
        ItemCopyCreateDto dto = new ItemCopyCreateDto();
        dto.setLibraryItemId(itemId);
        dto.setInventoryNumber("INV-001");

        LibraryItem item = new Book();

        when(libraryItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemCopyRepository.existsByInventoryNumber("INV-001")).thenReturn(false);
        when(itemCopyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ItemCopy result = itemCopyService.createCopy(dto);

        assertNotNull(result);
        assertEquals(ItemStatus.AVAILABLE, result.getStatus());
    }
}