package com.example.lims.entity.item;

import com.example.lims.entity.Location;
import com.example.lims.entity.Publisher;
import com.example.lims.entity.status.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "library_items")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "added_to_library_date", nullable = false)
    private LocalDate addedToLibraryDate;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToMany
    @JoinTable(
            name = "item_authors",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    public void addAuthor(Author author) {
        if (author != null && !authors.contains(author)) {
            authors.add(author);
        }
    }

    @PrePersist
    void onCreate() {
        if (addedToLibraryDate == null) {
            addedToLibraryDate = LocalDate.now();
        }
    }
}
