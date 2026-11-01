package com.example.lims.entity.item;

import com.example.lims.entity.status.ItemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_copies")
@Getter
@Setter
@NoArgsConstructor
public class ItemCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inventory_number", nullable = false, unique = true, length = 100)
    private String inventoryNumber;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private LibraryItem item;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private ItemStatus status;
}
