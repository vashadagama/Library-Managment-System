package com.example.lims.entity.status;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "audit_types")
public class AuditType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
