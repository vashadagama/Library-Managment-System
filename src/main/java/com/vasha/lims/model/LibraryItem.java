package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.UUID;
import main.java.com.vasha.lims.enums.ItemStatus;


public abstract class LibraryItem {
    protected UUID id; // Соответсвует PRIMARY KEY in PSQL
    protected String title;
    protected String location;
    protected String isbn;
    protected String publisher;
    protected LocalDate publicationDate;
    protected LocalDate addedToLibarayDate;
    protected Integer avaibleCopies;
    protected Integer totalCopies;
    protected ItemStatus status;


    //? Добавить необязательное задание даты публикации?
    public LibraryItem(String title, String publisher, LocalDate publicationDate, String isbn, String location, Integer totalCopies) {
        this.id = UUID.randomUUID(); //? точно ли при соед с бд рандом хороший вариант? возможно лучше исп некст номер
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        //! другие this.
    }
//!другие конструкторы, абср методы и тд

}