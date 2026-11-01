package com.example.lims.dto;

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

    private UUID publisherId;

    @NotNull(message = "Дата публикации обязательна")
    private LocalDate publicationDate;

    @Size(max = 20, message = "ISBN не должен превышать 20 символов")
    private String isbn;

    private UUID locationId;

    @NotNull(message = "Язык обязателен")
    private UUID languageId;

    @NotNull(message = "Жанр обязателен")
    private UUID genreId;

    @Positive(message = "Количество страниц должно быть положительным")
    private Integer pageCount;

    private List<UUID> authorIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getPublisherId() { return publisherId; }
    public void setPublisherId(UUID publisherId) { this.publisherId = publisherId; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
    public UUID getLanguageId() { return languageId; }
    public void setLanguageId(UUID languageId) { this.languageId = languageId; }
    public UUID getGenreId() { return genreId; }
    public void setGenreId(UUID genreId) { this.genreId = genreId; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public List<UUID> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<UUID> authorIds) { this.authorIds = authorIds; }
}
