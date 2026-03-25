package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.UUID;
import main.java.com.vasha.lims.enums.ItemStatus;
import static main.java.com.vasha.lims.util.ValidationUtil.*;


public abstract class LibraryItem {
    private UUID id; // Соответсвует PRIMARY KEY in PSQL
    private String title;
    private String location;
    private String isbn;
    private String language;
    private String publisher;
    private LocalDate publicationDate;
    private LocalDate addedToLibraryDate;
    private Integer availableCopies;
    private Integer totalCopies;
    private ItemStatus status;



    public LibraryItem(String title, String publisher, LocalDate publicationDate,
                       String isbn, String location, Integer totalCopies, String language) {
        setTitle(title);
        setPublisher(publisher);
        setPublicationDate(publicationDate);
        setIsbn(isbn);
        setLocation(location);
        setLanguage(language);
        
        if (totalCopies == null || totalCopies < 0) {
            throw new IllegalArgumentException("Количество копий не может быть отрицательным!");
        }

        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.status = ItemStatus.AVAILABLE;
        this.addedToLibraryDate = LocalDate.now();
        this.id = UUID.randomUUID(); 

    }

    public LibraryItem () {}

    public String getTitle() {
        return title;
    }

    public final void setTitle(String title){
        checkNotBlank(title, "Название");
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public final void setLocation(String location) {
        checkNotBlank(location, "Местонахождение");
        this.location = location;
    }

    public String getIsbn() {
        return isbn;
    }

    public final void setIsbn(String isbn) {
        checkNotBlank(isbn, "Код ISBN");
        this.isbn = isbn;
    }

    public String getLanguage() {
        return language;
    }

    public final void setLanguage(String language) {
        checkNotBlank(language, "Язык");
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public final void setPublisher(String publisher) {
        checkNotBlank(publisher, "Издатель");
        this.publisher = publisher;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public final void setPublicationDate(LocalDate publicationDate) {
        checkNotInFuture(publicationDate, "Дата публикации");
        this.publicationDate = publicationDate;
    }

    public LocalDate getAddedToLibraryDate() {
        return addedToLibraryDate;
    }

    public void setaddedToLibraryDate(LocalDate addedToLibraryDate) {
        checkNotInFuture(addedToLibraryDate, "Дата добавления в библиотеку");
        this.addedToLibraryDate = addedToLibraryDate;
    }



    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void incrementTotalCopies() {
        this.totalCopies += 1;
        this.availableCopies += 1;
    }

    public void decrementTotalCopies() {
        if (totalCopies > 0 && availableCopies > 0) {
            this.totalCopies -= 1;
            this.availableCopies -= 1;
        } else {
            throw new IllegalArgumentException("Невозможно списать книгу: доступных копий нет или фонд пуст.");
        }
    }

    public Integer getavailableCopies() {
        return availableCopies;
    }

    public void incrementavailableCopies() {
        if (availableCopies < totalCopies) {
            this.availableCopies += 1;
        } else {
            throw new IllegalArgumentException("Количество доступных копий не может превышать общее количество копий!");
        }
    }

    public void decrementavailableCopies() {
        if (availableCopies > 0) {
            this.availableCopies -= 1;
        } else {
            throw new IllegalArgumentException("Нет доступных копий для выдачи!");
        }
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        checkNotNull(status, "Статус предмета");
        this.status = status;
    }

    public abstract String getItemType();

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        LibraryItem item = (LibraryItem) object;

        return id != null && id.equals(item.id);

    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return 0;
    }
    

}

