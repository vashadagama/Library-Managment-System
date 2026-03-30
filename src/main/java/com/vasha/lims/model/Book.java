package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import main.java.com.vasha.lims.enums.BookGenre;
import static main.java.com.vasha.lims.util.ValidationUtil.*;

public class Book extends LibraryItem {

    private BookGenre genre;
    private Integer pageCount;

    public Book(String title, String publisher, LocalDate publicationDate,
                String isbn, String location,
                String language, BookGenre genre, Integer pageCount) {

        super(title, publisher, publicationDate,
                isbn, location, language);

        setGenre(genre);
        setPageCount(pageCount);


    }

    public Book() {}



    public BookGenre getGenre() {
        return genre;
    }

    public final void setGenre(BookGenre genre) {
        checkNotNull(genre, "Жанр");
        this.genre = genre;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public final void setPageCount(Integer pageCount) {
        checkIsPositive(pageCount, "");
        this.pageCount = pageCount;
    }

    @Override
    public String getItemType() {
        return "Книга";
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

