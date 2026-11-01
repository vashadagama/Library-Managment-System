package com.example.lims.repository;

import com.example.lims.entity.item.Magazine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MagazineRepository extends JpaRepository<Magazine, UUID> {

    List<Magazine> findByGenre_Id(UUID genreId);

    List<Magazine> findByPublisher_NameIgnoreCase(String publisherName);

    @Query("""
            select m from Magazine m
            where (cast(:title as String) is null or lower(m.title) like lower(concat('%', cast(:title as String), '%')))
              and (:genreId is null or m.genre.id = :genreId)
              and (:publisherId is null or m.publisher.id = :publisherId)
            """)
    Page<Magazine> searchMagazines(@Param("title") String title,
                                   @Param("genreId") UUID genreId,
                                   @Param("publisherId") UUID publisherId,
                                   Pageable pageable);
}
