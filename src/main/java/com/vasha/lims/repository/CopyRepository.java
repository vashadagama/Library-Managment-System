package main.java.com.vasha.lims.repository;

import main.java.com.vasha.lims.model.ItemCopy;
import main.java.com.vasha.lims.model.LibraryItem;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class CopyRepository {
    private final List<ItemCopy> copies = new ArrayList<>();



    public final void save(ItemCopy copy) {
        if (copy != null && !copies.contains(copy)) {
            copies.add(copy);
        }
    }

    public final ItemCopy findById(UUID id) {
        checkNotNull(id, "id экземпляра");
        for (ItemCopy copy : copies) {
            if (copy.getId().equals(id)) {
                return copy;
            }
        }
        return null;
    }

    public final ItemCopy findByInventoryNumber(String invNumber) {
        checkNotBlank(invNumber, "Инвентарный номер");
        for (ItemCopy copy : copies) {
            if (copy.getInventoryNumber().equals(invNumber)) {
                return copy;
            }
        }
        return null;
    }

    public final List<ItemCopy> findByItem(LibraryItem item) {
        checkNotNull(item, "Список экземпляров");
        List<ItemCopy> itemCopies = new ArrayList<>()
        for (ItemCopy copy : copies) {
            if (copy.getItem().equals(item)) {
                itemCopies.add(copy);
            }
        }
        return itemCopies;
    }


}
