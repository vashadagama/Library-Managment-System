package com.example.lims.repository;

import com.example.lims.entity.item.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {
    List<Author> findByLastNameIgnoreCase(String lastName);
}
