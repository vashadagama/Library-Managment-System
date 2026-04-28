package com.example.lims.repository;

import java.time.LocalDate;
import com.example.lims.model.Book;
import com.example.lims.enums.BookGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorsLastNameIgnoreCase(String lastName);
    List<Book> findByGenre(BookGenre genre);
    List<Book> findByPublisherIgnoreCase(String publisher);

    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR b.title ILIKE CONCAT('%', CAST(:title AS string), '%')) AND " +
            "(:genre IS NULL OR b.genre = :genre) AND " +
            "(:publisher IS NULL OR b.publisher ILIKE CONCAT('%', CAST(:publisher AS string), '%')) AND " +
            "(:yearFrom IS NULL OR b.publicationDate >= :yearFrom) AND " +
            "(:yearTo IS NULL OR b.publicationDate <= :yearTo)")
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("genre") BookGenre genre,
                           @Param("publisher") String publisher,
                           @Param("yearFrom") LocalDate yearFrom,
                           @Param("yearTo") LocalDate yearTo,
                           Pageable pageable);
}