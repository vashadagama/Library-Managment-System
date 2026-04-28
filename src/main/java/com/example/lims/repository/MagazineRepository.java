package com.example.lims.repository;

import com.example.lims.model.Magazine;
import com.example.lims.enums.MagazineGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MagazineRepository extends JpaRepository<Magazine, UUID> {
    @Query("SELECT m FROM Magazine m WHERE " +
            "(:title IS NULL OR m.title ILIKE CONCAT('%', CAST(:title AS string), '%')) AND " +
            "(:genre IS NULL OR m.genre = :genre) AND " +
            "(:publisher IS NULL OR m.publisher ILIKE CONCAT('%', CAST(:publisher AS string), '%'))")
    Page<Magazine> searchMagazines(@Param("title") String title,
                                   @Param("genre") MagazineGenre genre,
                                   @Param("publisher") String publisher,
                                   Pageable pageable);
}