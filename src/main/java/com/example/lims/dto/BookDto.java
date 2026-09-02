package com.example.lims.dto;

import com.example.lims.enums.BookGenre;
import com.example.lims.model.Author;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BookDto {
    private UUID id;
    private String title;
    private String publisher;
    private LocalDate publicationDate;
    private String isbn;
    private String location;
    private String language;
    private BookGenre genre;
    private Integer pageCount;
    private List<Author> authors;

    public BookDto() {}

    public BookDto(UUID id, String title, String publisher, LocalDate publicationDate,
                   String isbn, String location, String language, BookGenre genre,
                   Integer pageCount, List<Author> authors) {
        this.id = id;
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.isbn = isbn;
        this.location = location;
        this.language = language;
        this.genre = genre;
        this.pageCount = pageCount;
        this.authors = authors;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public BookGenre getGenre() { return genre; }
    public void setGenre(BookGenre genre) { this.genre = genre; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
}