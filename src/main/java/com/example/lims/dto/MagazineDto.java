package com.example.lims.dto;

import com.example.lims.enums.MagazineGenre;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MagazineDto {
    private UUID id;
    private String title;
    private String publisher;
    private LocalDate publicationDate;
    private String issn;
    private String location;
    private String language;
    private MagazineGenre genre;
    private Integer pageCount;
    private boolean hasGlossyCover;

    public MagazineDto() {}

    public MagazineDto(UUID id, String title, String publisher, LocalDate publicationDate,
                   String issn, String location, String language, MagazineGenre genre,
                   Integer pageCount, boolean hasGlossyCover) {
        this.id = id;
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.issn = issn;
        this.location = location;
        this.language = language;
        this.genre = genre;
        this.pageCount = pageCount;
        this.hasGlossyCover = hasGlossyCover;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public boolean getHasGlossyCover() { return hasGlossyCover; }
    public void setHasGlossyCover(boolean hasGlossyCover) { this.hasGlossyCover = hasGlossyCover; }

}