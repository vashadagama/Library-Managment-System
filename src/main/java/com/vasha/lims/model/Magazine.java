package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import main.java.com.vasha.lims.enums.MagazineGenre;
import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class Magazine extends LibraryItem {

    private MagazineGenre genre;
    private Integer pageCount;
    private boolean hasGlossyCover;

    public Magazine(String title, String publisher, LocalDate publicationDate,
                String isbn, String location, Integer totalCopies,
                String language, MagazineGenre genre, Integer pageCount, boolean hasGlossyCover) {

        super(title, publisher, publicationDate,
                isbn, location, totalCopies, language);

        setGenre(genre);
        setPageCount(pageCount);
        setHasGlossyCover(hasGlossyCover);

    }

    public Magazine() {}



    public MagazineGenre getGenre() {
        return genre;
    }

    public final void setGenre(MagazineGenre genre) {
        checkNotNull(genre, "Жанр");
        this.genre = genre;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public final void setPageCount(Integer pageCount) {
        checkIsPositive(pageCount, "Число страниц");
        this.pageCount = pageCount;
    }

    public boolean isHasGlossyCover() {
        return hasGlossyCover;
    }

    public final void setHasGlossyCover(boolean hasGlossyCover) {
        this.hasGlossyCover = hasGlossyCover;
    }

    @Override
    public String getItemType() {
        return "Журнал";
    }

    @Override
    public String toString() {
        String base = super.toString();
        return base.substring(0, base.length() - 1) +
                ", genre=" + genre.getDisplayName() +
                ", pageCount=" + pageCount +
                "}";

    }
}

