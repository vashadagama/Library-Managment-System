package com.example.lims.entity.item;

import com.example.lims.entity.status.Genre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Book extends LibraryItem {

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(name = "page_count")
    private Integer pageCount;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
