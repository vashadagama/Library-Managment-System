package com.example.lims.repository;

import com.example.lims.model.LibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LibraryItemRepository extends JpaRepository<LibraryItem, UUID> {
}