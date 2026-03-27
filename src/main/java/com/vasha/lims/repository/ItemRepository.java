package main.java.com.vasha.lims.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import main.java.com.vasha.lims.model.LibraryItem;
import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class ItemRepository {
    private final List<LibraryItem> items = new ArrayList<>();

    public final void save(LibraryItem item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
        }
    }

    public final LibraryItem findById(UUID id) {
        checkNotNull(id, "id экземпляра");
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public final LibraryItem findByIsbn(String isbn) {
        checkNotNull(isbn, "isbn экземпляра");
        for (LibraryItem item : items) {
            if (item.getIsbn().equals(isbn)) {
                return item;
            }
        }
        return null;
    }

    public final List<LibraryItem> findByTitle(String title) {
        checkNotBlank(title, "название экземпляра");
        List<LibraryItem> titleList = new ArrayList<>();

        for (LibraryItem item : items) {
            if (item.getTitle().equalsIgnoreCase(title)) {
                titleList.add(item);
            }
        }
        return titleList;
    }
}
