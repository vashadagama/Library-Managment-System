package com.example.lims.repository;

import com.example.lims.entity.status.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemStatusRepository extends JpaRepository<ItemStatus, UUID> {
    Optional<ItemStatus> findByNameIgnoreCase(String name);
}
