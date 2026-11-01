package com.example.lims.repository;

import com.example.lims.entity.item.ItemCopy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemCopyRepository extends JpaRepository<ItemCopy, UUID> {

    List<ItemCopy> findByItem_Id(UUID itemId);

    List<ItemCopy> findByItem_IdAndStatus_Id(UUID itemId, UUID statusId);

    List<ItemCopy> findByStatus_Id(UUID statusId);

    boolean existsByInventoryNumber(String inventoryNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from ItemCopy c
            where c.item.id = :itemId and c.status.name = :statusName
            order by c.id
            """)
    List<ItemCopy> findForUpdateByItemIdAndStatusName(@Param("itemId") UUID itemId,
                                                       @Param("statusName") String statusName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ItemCopy c where c.id = :id")
    Optional<ItemCopy> findByIdForUpdate(@Param("id") UUID id);
}
