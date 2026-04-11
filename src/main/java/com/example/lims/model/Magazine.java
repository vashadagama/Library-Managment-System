package com.example.lims.model;

import com.example.lims.enums.MagazineGenre;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "magazines")
public class Magazine extends LibraryItem {

    @Enumerated(EnumType.STRING)
    private MagazineGenre genre;

    private Integer pageCount;
    private boolean hasGlossyCover;

    public Magazine() {}

    public Magazine(String title, String publisher, LocalDate publicationDate,
                    String isbn, String location, String language,
                    MagazineGenre genre, Integer pageCount, boolean hasGlossyCover) {
        super(title, publisher, publicationDate, isbn, location, language);
        this.genre = genre;
        this.pageCount = pageCount;
        this.hasGlossyCover = hasGlossyCover;
    }

    @Override
    public String getItemType() { return "Журнал"; }

    public MagazineGenre getGenre() { return genre; }
    public void setGenre(MagazineGenre genre) { this.genre = genre; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public boolean isHasGlossyCover() { return hasGlossyCover; }
    public void setHasGlossyCover(boolean hasGlossyCover) { this.hasGlossyCover = hasGlossyCover; }
}