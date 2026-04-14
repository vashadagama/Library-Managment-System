package com.example.lims.model;

import com.example.lims.enums.BookGenre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book extends LibraryItem {

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Жанр книги обязателен")
    private BookGenre genre;

    @Positive(message = "Количество страниц должно быть положительным")
    private Integer pageCount;

    private String isbn;

    public Book() {}

    public Book(String title, String publisher, LocalDate publicationDate,
                String isbn, String location, String language,
                BookGenre genre, Integer pageCount) {
        super(title, publisher, publicationDate, location, language);
        this.isbn = isbn;
        this.genre = genre;
        this.pageCount = pageCount;
    }

    @Override
    public String getItemType() { return "Книга"; }

    public BookGenre getGenre() { return genre; }
    public void setGenre(BookGenre genre) { this.genre = genre; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
}