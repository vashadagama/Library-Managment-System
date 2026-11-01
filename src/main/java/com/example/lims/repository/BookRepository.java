package com.example.lims.repository;

import com.example.lims.entity.item.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByGenre_Id(UUID genreId);

    List<Book> findByPublisher_NameIgnoreCase(String publisherName);

    @Query("""
            select distinct b from Book b
            join b.authors a
            where lower(a.lastName) like lower(concat('%', :lastName, '%'))
            """)
    List<Book> findByAuthorLastName(@Param("lastName") String lastName);

    @Query("""
            select b from Book b
            where (cast(:title as String) is null or lower(b.title) like lower(concat('%', cast(:title as String), '%')))
              and (:genreId is null or b.genre.id = :genreId)
              and (:publisherId is null or b.publisher.id = :publisherId)
              and b.publicationDate >= coalesce(:yearFrom, b.publicationDate)
              and b.publicationDate <= coalesce(:yearTo, b.publicationDate)
            """)
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("genreId") UUID genreId,
                           @Param("publisherId") UUID publisherId,
                           @Param("yearFrom") LocalDate yearFrom,
                           @Param("yearTo") LocalDate yearTo,
                           Pageable pageable);
}
