package com.example.lims.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FineDto {

    private UUID id;
    private BigDecimal amount;
    private String reason;
    private Boolean isPaid;
    private UUID loanId;
    private String userFullName;
    private String bookTitle;
    private String inventoryNumber;
    private LocalDate dueDate;
    private LocalDate returnDate;

    public FineDto() {
    }

    public FineDto(UUID id, BigDecimal amount, String reason, Boolean isPaid, UUID loanId,
                   String userFullName, String bookTitle, String inventoryNumber,
                   LocalDate dueDate, LocalDate returnDate) {
        this.id = id;
        this.amount = amount;
        this.reason = reason;
        this.isPaid = isPaid;
        this.loanId = loanId;
        this.userFullName = userFullName;
        this.bookTitle = bookTitle;
        this.inventoryNumber = inventoryNumber;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}
