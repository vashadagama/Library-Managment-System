package com.example.lims.dto;

import com.example.lims.enums.LoanStatus;
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
    private LoanStatus status;

    public LoanDto(UUID id, UUID userId, String userFullName, UUID copyId, String inventoryNumber,
                   String bookTitle, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, LoanStatus status) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.copyId = copyId;
        this.inventoryNumber = inventoryNumber;
        this.bookTitle = bookTitle;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getUserFullName() { return userFullName; }
    public UUID getCopyId() { return copyId; }
    public String getInventoryNumber() { return inventoryNumber; }
    public String getBookTitle() { return bookTitle; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LoanStatus getStatus() { return status; }
}