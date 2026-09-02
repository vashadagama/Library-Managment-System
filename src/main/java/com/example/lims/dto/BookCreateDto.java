package com.example.lims.dto;

import com.example.lims.enums.BookGenre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BookCreateDto {

    @NotBlank(message = "Название обязательно")
    private String title;

    private String publisher;

    @NotNull(message = "Дата публикации обязательна")
    private LocalDate publicationDate;

    @Size(max = 20, message = "ISBN не должен превышать 20 символов")
    private String isbn;

    private String location;

    @NotBlank(message = "Язык обязателен")
    private String language;

    @NotNull(message = "Жанр обязателен")
    private BookGenre genre;

    @Positive(message = "Количество страниц должно быть положительным")
    private Integer pageCount;

    private List<UUID> authorIds;

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
    public List<UUID> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<UUID> authorIds) { this.authorIds = authorIds; }
}