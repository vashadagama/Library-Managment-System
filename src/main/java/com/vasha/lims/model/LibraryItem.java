package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.UUID;
import main.java.com.vasha.lims.enums.ItemStatus;
import static main.java.com.vasha.lims.util.ValidationUtil.checkNotBlank;
import static main.java.com.vasha.lims.util.ValidationUtil.checkNotInFuture;


public abstract class LibraryItem {
    private UUID id; // Соответсвует PRIMARY KEY in PSQL
    private String title;
    private String location;
    private String isbn;
    private String language;
    private String publisher;
    private LocalDate publicationDate;
    private LocalDate addedToLibaryDate;
    private Integer avaibleCopies;
    private Integer totalCopies;
    private ItemStatus status;



    public LibraryItem(String title, String publisher, LocalDate publicationDate, String isbn, String location, Integer totalCopies) {
        setTitle(title);
        setPublisher(publisher);
        setPublicationDate(publicationDate);
        setIsbn(isbn);
        setLocation(location);
        setLanguage(language);
        
//copies  осталос, пока не понятно
        this.totalCopies = totalCopies;
        this.avaibleCopies = totalCopies;
        this.status = ItemStatus.AVAILABLE;
        this.addedToLibaryDate = LocalDate.now();
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
        return addedToLibaryDate;
    }

    public void setAddedToLibaryDate(LocalDate addedToLibaryDate) {
        checkNotInFuture(addedToLibaryDate, "Дата добавления в библиотеку");
        this.addedToLibaryDate = addedToLibaryDate;
    }



    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void incrementTotalCopies() {
            this.avaibleCopies += 1;
    }

    public void dicrementTotalCopies() {
        if (totalCopies - 1 >= 0){
            this.avaibleCopies -= 1;
        } else {
            throw new IllegalArgumentException("Количество копий не может быть меньше нуля!");
        }
    }

    public Integer getAvaibleCopies() {
        return avaibleCopies;
    }

    public void incrementAvaibleCopies() {
        if (avaibleCopies + 1 <= totalCopies){
            this.avaibleCopies += 1;
        } else {
            throw new IllegalArgumentException("Количество доступных копий не может превышать общее количество копий!");
        }
    }

    public void dicrementAvaibleCopies() {
        if (avaibleCopies - 1 >= 0){
            this.avaibleCopies -= 1;
        } else {
            throw new IllegalArgumentException("Количество доступных копий не может быть меньше нуля!");
        }
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setItemStatus(ItemStatus status) {
        //??? какая тут нужна проверка?
    }

    

}

