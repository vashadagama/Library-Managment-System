package main.java.com.vasha.lims.model;

import main.java.com.vasha.lims.enums.ItemStatus;

import java.util.UUID;

public class ItemCopy {
    private UUID id;
    private LibraryItem item;
    private String inventoryNumber;
    private ItemStatus status;

    public ItemCopy(LibraryItem item, String inventoryNumber) {
        this.item = item;
        this.inventoryNumber = inventoryNumber;
        this.id = UUID.randomUUID();
        this.status = ItemStatus.AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public LibraryItem getItem() {
        return item;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }
    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ItemCopy item = (ItemCopy) object;

        return id != null && id.equals(item.id);

    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "ItemCopy " + "{" +
                "id=" + id +
                ", item=" + item +
                ", inventoryNumber='" + inventoryNumber + '\'' +
                ", status=" + status +
                '}';
    }
}
