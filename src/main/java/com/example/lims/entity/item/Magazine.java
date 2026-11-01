package com.example.lims.entity.item;

import com.example.lims.entity.status.Genre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "magazines")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Magazine extends LibraryItem {

    @Column(unique = true, length = 20)
    private String issn;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "has_glossy_cover")
    private Boolean hasGlossyCover = false;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
