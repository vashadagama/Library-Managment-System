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
            "(:genre IS NULL OR m.genre = :genre) AND " +
            "(:publisher IS NULL OR LOWER(m.publisher) LIKE LOWER(CONCAT('%', :publisher, '%')))")
    Page<Magazine> searchMagazines(@Param("genre") MagazineGenre genre,
                                   @Param("publisher") String publisher,
                                   Pageable pageable);
}