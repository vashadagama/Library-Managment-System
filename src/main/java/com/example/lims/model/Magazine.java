package com.example.lims.model;

import com.example.lims.enums.MagazineGenre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
@Table(name = "magazines")
public class Magazine extends LibraryItem {

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Жанр журнала обязателен")
    private MagazineGenre genre;

    @Positive(message = "Количество страниц должно быть положительным")
    private Integer pageCount;

    private boolean hasGlossyCover;
    private String issn;

    public Magazine() {}

    public Magazine(String title, String publisher, LocalDate publicationDate,
                    String issn, String location, String language,
                    MagazineGenre genre, Integer pageCount, boolean hasGlossyCover) {
        super(title, publisher, publicationDate, location, language);
        this.issn = issn;
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
    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }
}