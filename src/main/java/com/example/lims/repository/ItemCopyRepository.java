package com.example.lims.repository;

import com.example.lims.model.ItemCopy;
import com.example.lims.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemCopyRepository extends JpaRepository<ItemCopy, UUID> {
    List<ItemCopy> findByItemIdAndStatus(UUID itemId, ItemStatus status);
    List<ItemCopy> findByItemId(UUID itemId);
    List<ItemCopy> findByStatus(ItemStatus status);
}