package com.example.lims.model;

import com.example.lims.enums.ItemStatus;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "item_copies")
public class ItemCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private LibraryItem item;

    private String inventoryNumber;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

    public ItemCopy() {}

    public ItemCopy(LibraryItem item, String inventoryNumber) {
        this.item = item;
        this.inventoryNumber = inventoryNumber;
    }

    public UUID getId() { return id; }
    public LibraryItem getItem() { return item; }
    public void setItem(LibraryItem item) { this.item = item; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
}