package com.example.lims.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "library_items")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String location;
    private String isbn;
    private String language;
    private String publisher;
    private LocalDate publicationDate;
    private LocalDate addedToLibraryDate = LocalDate.now();


    @ManyToMany
    @JoinTable(
            name = "item_authors",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    public LibraryItem() {}

    public LibraryItem(String title, String publisher, LocalDate publicationDate,
                       String isbn, String location, String language) {
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.isbn = isbn;
        this.location = location;
        this.language = language;
    }

    public void addAuthor(Author author) {
        if (author != null && !this.authors.contains(author)) {
            this.authors.add(author);
            author.addWork(this);
        }
    }

    public abstract String getItemType();


    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public LocalDate getAddedToLibraryDate() { return addedToLibraryDate; }
    public void setAddedToLibraryDate(LocalDate addedToLibraryDate) { this.addedToLibraryDate = addedToLibraryDate; }
    public List<Author> getAuthors() { return authors; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LibraryItem item = (LibraryItem) object;
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}