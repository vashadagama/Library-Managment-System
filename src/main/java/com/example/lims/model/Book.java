package com.example.lims.model;

import com.example.lims.enums.BookGenre;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book extends LibraryItem {

    @Enumerated(EnumType.STRING)
    private BookGenre genre;

    private Integer pageCount;

    public Book() {}

    public Book(String title, String publisher, LocalDate publicationDate,
                String isbn, String location, String language,
                BookGenre genre, Integer pageCount) {
        super(title, publisher, publicationDate, isbn, location, language);
        this.genre = genre;
        this.pageCount = pageCount;
    }

    @Override
    public String getItemType() { return "Книга"; }

    public BookGenre getGenre() { return genre; }
    public void setGenre(BookGenre genre) { this.genre = genre; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
}