package com.example.lims.dto;

import java.time.LocalDate;
import java.util.UUID;

public class LoanDto {

    private UUID id;
    private UUID userId;
    private String userFullName;
    private UUID copyId;
    private String inventoryNumber;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private UUID statusId;
    private String statusName;
    private java.math.BigDecimal fineAmount;

    public LoanDto() {
    }

    public LoanDto(UUID id, UUID userId, String userFullName, UUID copyId, String inventoryNumber,
                   String bookTitle, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate,
                   UUID statusId, String statusName) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.copyId = copyId;
        this.inventoryNumber = inventoryNumber;
        this.bookTitle = bookTitle;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.statusId = statusId;
        this.statusName = statusName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public UUID getCopyId() { return copyId; }
    public void setCopyId(UUID copyId) { this.copyId = copyId; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public UUID getStatusId() { return statusId; }
    public void setStatusId(UUID statusId) { this.statusId = statusId; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public java.math.BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(java.math.BigDecimal fineAmount) { this.fineAmount = fineAmount; }
}
