package com.example.lims.entity.loan;

import com.example.lims.entity.item.ItemCopy;
import com.example.lims.entity.status.LoanStatus;
import com.example.lims.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount = 0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "librarian_id")
    private User librarian;

    @ManyToOne
    @JoinColumn(name = "copy_id", nullable = false)
    private ItemCopy copy;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private LoanStatus status;

    @PrePersist
    void onCreate() {
        if (loanDate == null) {
            loanDate = LocalDate.now();
        }
        if (renewalCount == null) {
            renewalCount = 0;
        }
    }
}
