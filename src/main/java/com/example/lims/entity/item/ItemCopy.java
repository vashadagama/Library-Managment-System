package com.example.lims.entity.item;

import com.example.lims.entity.status.ItemStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_copies")
public class ItemCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String inventoryNumber;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column()
    private Float price;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private UUID item;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private ItemStatus status;
}