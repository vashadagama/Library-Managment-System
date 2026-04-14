package com.example.lims.controller;

import com.example.lims.dto.ItemCopyCreateDto;
import com.example.lims.dto.ItemCopyDto;
import com.example.lims.dto.ItemCopyStatusUpdateDto;
import com.example.lims.enums.ItemStatus;
import com.example.lims.model.ItemCopy;
import com.example.lims.service.ItemCopyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-copies")
public class ItemCopyController {

    private final ItemCopyService itemCopyService;

    public ItemCopyController(ItemCopyService itemCopyService) {
        this.itemCopyService = itemCopyService;
    }

    @PostMapping
    public ItemCopyDto create(@Valid @RequestBody ItemCopyCreateDto dto) {
        ItemCopy copy = itemCopyService.createCopy(dto);
        return toDto(copy);
    }

    @GetMapping("/{id}")
    public ItemCopyDto getById(@PathVariable UUID id) {
        ItemCopy copy = itemCopyService.getById(id);
        return toDto(copy);
    }

    @GetMapping
    public List<ItemCopyDto> getAll(
            @RequestParam(required = false) UUID libraryItemId,
            @RequestParam(required = false) ItemStatus status
    ) {
        List<ItemCopy> copies;
        if (libraryItemId != null && status != null) {
            copies = itemCopyService.getByLibraryItemAndStatus(libraryItemId, status);
        } else if (libraryItemId != null) {
            copies = itemCopyService.getByLibraryItem(libraryItemId);
        } else if (status != null) {
            copies = itemCopyService.getByStatus(status);
        } else {
            copies = itemCopyService.getAllCopies();
        }
        return copies.stream().map(this::toDto).toList();
    }

    @PatchMapping("/{id}/status")
    public ItemCopyDto updateStatus(@PathVariable UUID id, @Valid @RequestBody ItemCopyStatusUpdateDto dto) {
        ItemCopy updated = itemCopyService.updateStatus(id, dto.getStatus());
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        itemCopyService.deleteCopy(id);
    }

    private ItemCopyDto toDto(ItemCopy copy) {
        return new ItemCopyDto(
                copy.getId(),
                copy.getInventoryNumber(),
                copy.getStatus(),
                copy.getItem() != null ? copy.getItem().getId() : null,
                copy.getItem() != null ? copy.getItem().getTitle() : null
        );
    }
}