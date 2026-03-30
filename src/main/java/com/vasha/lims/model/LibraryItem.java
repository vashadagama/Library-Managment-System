package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static main.java.com.vasha.lims.util.ValidationUtil.*;


public abstract class LibraryItem {
    private UUID id;
    private String title;
    private String location;
    private String isbn;
    private String language;
    private String publisher;
    private LocalDate publicationDate;
    private LocalDate addedToLibraryDate;
    private final List<Author> authors = new ArrayList<>();

    public LibraryItem(String title, String publisher, LocalDate publicationDate,
                       String isbn, String location, String language) {
        setTitle(title);
        setPublisher(publisher);
        setPublicationDate(publicationDate);
        setIsbn(isbn);
        setLocation(location);
        setLanguage(language);

        this.addedToLibraryDate = LocalDate.now();
        this.id = UUID.randomUUID();

    }

    public LibraryItem () {}


    public String getTitle() {
        return title;
    }

    public final void setTitle(String title){
        checkNotBlank(title, "Название");
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public final void setLocation(String location) {
        checkNotBlank(location, "Местонахождение");
        this.location = location;
    }

    public String getIsbn() {
        return isbn;
    }

    public final void setIsbn(String isbn) {
        checkNotBlank(isbn, "Код ISBN");
        this.isbn = isbn;
    }

    public String getLanguage() {
        return language;
    }

    public final void setLanguage(String language) {
        checkNotBlank(language, "Язык");
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public final void setPublisher(String publisher) {
        checkNotBlank(publisher, "Издатель");
        this.publisher = publisher;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public final void setPublicationDate(LocalDate publicationDate) {
        checkNotInFuture(publicationDate, "Дата публикации");
        this.publicationDate = publicationDate;
    }

    public LocalDate getAddedToLibraryDate() {
        return addedToLibraryDate;
    }

    public final void setAddedToLibraryDate(LocalDate addedToLibraryDate) {
        checkNotInFuture(addedToLibraryDate, "Дата добавления в библиотеку");
        this.addedToLibraryDate = addedToLibraryDate;
    }

    public UUID getId() {
        return id;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void addAuthor(Author author) {
        if (author != null && !this.authors.contains(author)) {
            this.authors.add(author);
            author.addWork(this);
        }
    }

    public abstract String getItemType();

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        LibraryItem item = (LibraryItem) object;

        return id != null && id.equals(item.id);

    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return getItemType() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", isbn='" + isbn + '\'' +
                ", language='" + language + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publicationDate=" + publicationDate +
                ", addedDate=" + addedToLibraryDate +
                ", authorsCount=" + authors.size() +
                '}';
    }
}

