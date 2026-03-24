package main.java.com.vasha.lims.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Author {
    private UUID id;
    private String Name;
    private LocalDate birthdate;
    private List<Book> books;


    public Author(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.books = new ArrayList<>();
        .....
    }
}
