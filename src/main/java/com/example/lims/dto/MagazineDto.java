package com.example.lims.dto;

import java.time.LocalDate;
import java.util.UUID;

public class MagazineDto {

    private UUID id;
    private String title;
    private UUID publisherId;
    private String publisherName;
    private LocalDate publicationDate;
    private String issn;
    private UUID locationId;
    private String locationName;
    private UUID languageId;
    private String languageName;
    private UUID genreId;
    private String genreName;
    private Integer pageCount;
    private boolean hasGlossyCover;

    public MagazineDto() {
    }

    public MagazineDto(UUID id, String title, UUID publisherId, String publisherName,
                       LocalDate publicationDate, String issn, UUID locationId, String locationName,
                       UUID languageId, String languageName, UUID genreId, String genreName,
                       Integer pageCount, boolean hasGlossyCover) {
        this.id = id;
        this.title = title;
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.publicationDate = publicationDate;
        this.issn = issn;
        this.locationId = locationId;
        this.locationName = locationName;
        this.languageId = languageId;
        this.languageName = languageName;
        this.genreId = genreId;
        this.genreName = genreName;
        this.pageCount = pageCount;
        this.hasGlossyCover = hasGlossyCover;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getPublisherId() { return publisherId; }
    public void setPublisherId(UUID publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }
    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public UUID getLanguageId() { return languageId; }
    public void setLanguageId(UUID languageId) { this.languageId = languageId; }
    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    public UUID getGenreId() { return genreId; }
    public void setGenreId(UUID genreId) { this.genreId = genreId; }
    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public boolean isHasGlossyCover() { return hasGlossyCover; }
    public void setHasGlossyCover(boolean hasGlossyCover) { this.hasGlossyCover = hasGlossyCover; }
}
