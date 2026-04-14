package com.example.lims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ItemCopyCreateDto {

    @NotNull(message = "ID издания обязателен")
    private UUID libraryItemId;

    @NotBlank(message = "Инвентарный номер обязателен")
    private String inventoryNumber;

    public UUID getLibraryItemId() {
        return libraryItemId;
    }

    public void setLibraryItemId(UUID libraryItemId) {
        this.libraryItemId = libraryItemId;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }
}