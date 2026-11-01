package com.example.lims.repository;

import com.example.lims.entity.status.AuditType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditTypeRepository extends JpaRepository<AuditType, UUID> {
    Optional<AuditType> findByNameIgnoreCase(String name);
}
