package com.example.lims.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String hall;

    @Column(length = 100)
    private String shelf;

    @Column(length = 100)
    private String rack;
}
