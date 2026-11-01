package com.example.lims.repository;

import com.example.lims.entity.status.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, UUID> {
    Optional<NotificationType> findByNameIgnoreCase(String name);
}
