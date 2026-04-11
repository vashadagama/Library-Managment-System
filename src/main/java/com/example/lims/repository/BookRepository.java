package com.example.lims.repository;

import java.util.List;
import com.example.lims.model.Book;
import com.example.lims.enums.BookGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorsLastNameIgnoreCase(String lastName);
    List<Book> findByGenre(BookGenre genre);
    List<Book> findByPublisherIgnoreCase(String publisher);
}