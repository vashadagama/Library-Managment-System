package com.example.lims.dto;

import com.example.lims.enums.ItemStatus;
import java.util.UUID;

public class ItemCopyDto {
    private UUID id;
    private String inventoryNumber;
    private ItemStatus status;
    private UUID libraryItemId;
    private String libraryItemTitle;

    public ItemCopyDto(UUID id, String inventoryNumber, ItemStatus status, UUID libraryItemId, String libraryItemTitle) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.status = status;
        this.libraryItemId = libraryItemId;
        this.libraryItemTitle = libraryItemTitle;
    }

    public UUID getId() { return id; }
    public String getInventoryNumber() { return inventoryNumber; }
    public ItemStatus getStatus() { return status; }
    public UUID getLibraryItemId() { return libraryItemId; }
    public String getLibraryItemTitle() { return libraryItemTitle; }
}