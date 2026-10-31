package com.example.lims.entity.loan;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "fines")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "reason")
    private String reason;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
}
