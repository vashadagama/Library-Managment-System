package com.example.lims.dto;

import java.util.UUID;

public class PopularBookDto {
    private UUID libraryItemId;
    private String title;
    private String authors;
    private long loanCount;

    public PopularBookDto(UUID libraryItemId, String title, String authors, long loanCount) {
        this.libraryItemId = libraryItemId;
        this.title = title;
        this.authors = authors;
        this.loanCount = loanCount;
    }

    public UUID getLibraryItemId() { return libraryItemId; }
    public String getTitle() { return title; }
    public String getAuthors() { return authors; }
    public long getLoanCount() { return loanCount; }
}