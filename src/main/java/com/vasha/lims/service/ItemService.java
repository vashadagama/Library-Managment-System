package main.java.com.vasha.lims.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import main.java.com.vasha.lims.enums.BookGenre;
import main.java.com.vasha.lims.enums.ItemStatus;
import main.java.com.vasha.lims.enums.MagazineGenre;
import main.java.com.vasha.lims.model.Book;
import main.java.com.vasha.lims.model.LibraryItem;
import main.java.com.vasha.lims.model.Magazine;
import main.java.com.vasha.lims.repository.ItemRepository;

public class ItemService {
    private final ItemRepository itemRepo;

    public ItemService(ItemRepository itemRepo){
        this.itemRepo = itemRepo;
    }

// Метод registerBook(...) / registerMagazine(...):

// Логика:
// Проверяем по ISBN (itemRepo.findByIsbn), нет ли уже такого издания в базе.
// Если есть — мы не создаем новую книгу, а просто бросаем IllegalStateException("Издание с таким ISBN уже есть в фонде. Используйте метод добавления копий.").
// Если нет — создаем объект, сохраняем в репозиторий, возвращаем объект.

    public final Book registerBook(String title, String publisher, LocalDate publicationDate,
        String isbn, String location, Integer totalCopies,
        String language, BookGenre genre, Integer pageCount) {

        if (itemRepo.findByIsbn(isbn) != null) {
            throw new IllegalStateException("Издание с таким ISBN уже есть в фонде. Используйте метод добавления копий.");
        }
        Book book = new Book(title, publisher, publicationDate, 
                isbn, location, totalCopies,
                language, genre, pageCount);
            itemRepo.save(book);
            return book;
    }

    public final Magazine registerMagazine(String title, String publisher, LocalDate publicationDate,
        String isbn, String location, Integer totalCopies,
        String language, MagazineGenre genre, Integer pageCount, boolean hasGlossyCover) {

        if (itemRepo.findByIsbn(isbn) != null) {
            throw new IllegalStateException("Издание с таким ISBN уже есть в фонде. Используйте метод добавления копий.");
        }
        Magazine magazine = new Magazine(title, publisher, publicationDate, 
                isbn, location, totalCopies,
                language, genre, pageCount, hasGlossyCover);
        itemRepo.save(magazine);
        return magazine;
    }

    public final void addCopiesToItem(UUID itemId, int amount) {
        LibraryItem item = itemRepo.findById(itemId);
        if (item == null) { 
            throw new IllegalArgumentException("Экземпляр не найден!"); 
        }
        for (int number = 0; number < amount; number++) {
            item.incrementTotalCopies();
        }
    }

    public final void writeOffItem(UUID itemId) {
        LibraryItem item = itemRepo.findById(itemId);

        if (item == null) { 
            throw new IllegalArgumentException("Экземпляр не найден!"); 
        }

        item.decrementTotalCopies();
        
        if (item.getTotalCopies() < 1) {
            item.setStatus(ItemStatus.WITHDRAWN);
        }
        
       
    }

    public final LibraryItem findItemById(UUID itemId) {
        return itemRepo.findById(itemId);
    }

    public final List<LibraryItem> findItemByTitle(String title) {
        return itemRepo.findByTitle(title);
    }

}

