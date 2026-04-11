package com.example.lims.model;

import com.example.lims.enums.LoanStatus;
import com.example.lims.enums.ItemStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "copy_id")
    private ItemCopy copy;

    private LocalDate loanDate = LocalDate.now();
    private LocalDate dueDate = LocalDate.now().plusDays(14);
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;

    public Loan() {}

    public Loan(User user, ItemCopy copy) {
        this.user = user;
        this.copy = copy;
        this.copy.setStatus(ItemStatus.BORROVED);
    }

    public void returnItem() {
        if (this.status == LoanStatus.RETURNED) return;
        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
        this.copy.setStatus(ItemStatus.AVAILABLE);
    }

    public void renewLoan(Integer extraDays) {
        if (this.status == LoanStatus.ACTIVE) {
            this.dueDate = this.dueDate.plusDays(extraDays);
            this.status = LoanStatus.RENEWED;
        } else {
            throw new IllegalStateException("Нельзя продлить книгу со статусом: " + status);
        }
    }

    public void setStatus(LoanStatus status) { this.status = status; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ItemCopy getCopy() { return copy; }
    public void setCopy(ItemCopy copy) { this.copy = copy; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LoanStatus getStatus() { return status; }
}