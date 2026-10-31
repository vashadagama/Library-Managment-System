package com.example.lims.entity.status;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "item_statuses")
public class ItemStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
