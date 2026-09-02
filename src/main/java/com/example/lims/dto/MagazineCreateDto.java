package com.example.lims.dto;

import com.example.lims.enums.MagazineGenre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class MagazineCreateDto {

    @NotBlank(message = "Название обязательно")
    private String title;

    private String publisher;

    @NotNull(message = "Дата публикации обязательна")
    private LocalDate publicationDate;

    @Size(max = 20, message = "ISSN не должен превышать 20 символов")
    private String issn;

    private String location;

    @NotBlank(message = "Язык обязателен")
    private String language;

    @NotNull(message = "Жанр обязателен")
    private MagazineGenre genre;

    @Positive(message = "Количество страниц должно быть положительным")
    private Integer pageCount;

    private boolean hasGlossyCover;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public MagazineGenre getGenre() { return genre; }
    public void setGenre(MagazineGenre genre) { this.genre = genre; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public boolean isHasGlossyCover() { return hasGlossyCover; }
    public void setHasGlossyCover(boolean hasGlossyCover) { this.hasGlossyCover = hasGlossyCover; }
}