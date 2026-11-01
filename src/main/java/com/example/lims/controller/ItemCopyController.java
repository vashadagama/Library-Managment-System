package com.example.lims.controller;

import com.example.lims.dto.ItemCopyCreateDto;
import com.example.lims.dto.ItemCopyDto;
import com.example.lims.dto.ItemCopyStatusUpdateDto;
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
        return itemCopyService.createCopy(dto);
    }

    @GetMapping("/{id}")
    public ItemCopyDto getById(@PathVariable UUID id) {
        return itemCopyService.getById(id);
    }

    @GetMapping
    public List<ItemCopyDto> getAll(
            @RequestParam(required = false) UUID libraryItemId,
            @RequestParam(required = false) String status
    ) {
        if (libraryItemId != null && status != null) {
            return itemCopyService.getByLibraryItemAndStatus(libraryItemId, status);
        } else if (libraryItemId != null) {
            return itemCopyService.getByLibraryItem(libraryItemId);
        } else if (status != null) {
            return itemCopyService.getByStatus(status);
        }
        return itemCopyService.getAllCopies();
    }

    @PatchMapping("/{id}/status")
    public ItemCopyDto updateStatus(@PathVariable UUID id, @Valid @RequestBody ItemCopyStatusUpdateDto dto) {
        return itemCopyService.updateStatus(id, dto.getStatus());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        itemCopyService.deleteCopy(id);
    }
}
