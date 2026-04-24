package com.example.lims.repository;

import java.time.LocalDate;
import java.util.List;
import com.example.lims.model.Book;
import com.example.lims.enums.BookGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorsLastNameIgnoreCase(String lastName);
    List<Book> findByGenre(BookGenre genre);
    List<Book> findByPublisherIgnoreCase(String publisher);

    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:genre IS NULL OR b.genre = :genre) AND " +
            "(:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
            "(:yearFrom IS NULL OR b.publicationDate >= :yearFrom) AND " +
            "(:yearTo IS NULL OR b.publicationDate <= :yearTo)")
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("genre") BookGenre genre,
                           @Param("publisher") String publisher,
                           @Param("yearFrom") LocalDate yearFrom,
                           @Param("yearTo") LocalDate yearTo,
                           Pageable pageable);
}