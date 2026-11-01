package com.example.lims.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ItemCopyDto {

    private UUID id;
    private String inventoryNumber;
    private UUID statusId;
    private String statusName;
    private UUID libraryItemId;
    private String libraryItemTitle;
    private LocalDate acquisitionDate;
    private BigDecimal price;

    public ItemCopyDto() {
    }

    public ItemCopyDto(UUID id, String inventoryNumber, UUID statusId, String statusName,
                       UUID libraryItemId, String libraryItemTitle,
                       LocalDate acquisitionDate, BigDecimal price) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.statusId = statusId;
        this.statusName = statusName;
        this.libraryItemId = libraryItemId;
        this.libraryItemTitle = libraryItemTitle;
        this.acquisitionDate = acquisitionDate;
        this.price = price;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public UUID getStatusId() { return statusId; }
    public void setStatusId(UUID statusId) { this.statusId = statusId; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public UUID getLibraryItemId() { return libraryItemId; }
    public void setLibraryItemId(UUID libraryItemId) { this.libraryItemId = libraryItemId; }
    public String getLibraryItemTitle() { return libraryItemTitle; }
    public void setLibraryItemTitle(String libraryItemTitle) { this.libraryItemTitle = libraryItemTitle; }
    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
