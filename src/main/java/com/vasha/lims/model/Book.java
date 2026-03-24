package main.java.com.vasha.lims.model;

import java.util.ArrayList;
import java.util.List;
import main.java.com.vasha.lims.enums.BookGenre;
import main.java.com.vasha.lims.model.Author;


public class Book extends LibraryItem {
    private List<Author> authors;
    private BookGenre genre;
    private Integer pageCount;
    private Integer edition;
    private String series;


    public Book(String title, String publisher, LocalDate publicationDate,
                String isbn, String location, int totalCopies,
                BookGenre genre, int pageCount, String language) {

        super(title, publisher, publicationDate, isbn, location, totalCopies);
        this.authors = new ArrayList<>();
        this.genre = genre;
        this.pageCount = pageCount;
        this.language = language;
        this.edition = 1;
    }

}

