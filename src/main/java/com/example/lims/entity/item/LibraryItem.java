package com.example.lims.entity.item;

import com.example.lims.entity.Location;
import com.example.lims.entity.status.Language;
import com.example.lims.entity.Publisher;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "library_items")
public class LibraryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "added_to_libraerry_date")
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

    @PrePersist
    void onCreate(){
        if (addedToLibraryDate == null) {
            addedToLibraryDate = LocalDate.now();
        }
    }
}