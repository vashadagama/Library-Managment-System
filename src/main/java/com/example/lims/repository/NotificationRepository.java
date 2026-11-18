package com.example.lims.repository;

import com.example.lims.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderBySentAtDesc(UUID userId);

    List<Notification> findTop50ByOrderBySentAtDesc();

    boolean existsByUserIdAndTypeIdAndMessage(UUID userId, UUID typeId, String message);
}
