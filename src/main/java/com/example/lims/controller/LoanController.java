package com.example.lims.controller;

import com.example.lims.dto.LoanDto;
import com.example.lims.dto.PopularBookDto;
import com.example.lims.model.Loan;
import com.example.lims.service.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/issue/by-item")
    public LoanDto issueByLibraryItem(@RequestParam UUID userId, @RequestParam UUID libraryItemId) {
        Loan loan = loanService.issueBookByLibraryItem(userId, libraryItemId);
        return toDto(loan);
    }

    @PostMapping("/issue/by-copy")
    public LoanDto issueByCopy(@RequestParam UUID userId, @RequestParam UUID copyId) {
        Loan loan = loanService.issueBookByCopy(userId, copyId);
        return toDto(loan);
    }

    @PostMapping("/return")
    public LoanDto returnBook(@RequestParam UUID loanId) {
        Loan loan = loanService.returnBook(loanId);
        return toDto(loan);
    }

    @GetMapping("/overdue")
    public List<LoanDto> getOverdue() {
        return loanService.getOverdueLoans().stream().map(this::toDto).toList();
    }

    @PostMapping("/{loanId}/renew")
    public LoanDto renewLoan(@PathVariable UUID loanId, @RequestParam(defaultValue = "14") int extraDays) {
        Loan loan = loanService.renewLoan(loanId, extraDays);
        return toDto(loan);
    }

    // ---------- Новые эндпоинты для истории и статистики ----------

    @GetMapping("/history/user/{userId}")
    public List<LoanDto> getUserHistory(@PathVariable UUID userId) {
        return loanService.getLoansByUser(userId).stream().map(this::toDto).toList();
    }

    @GetMapping("/history/copy/{copyId}")
    public List<LoanDto> getCopyHistory(@PathVariable UUID copyId) {
        return loanService.getLoansByCopy(copyId).stream().map(this::toDto).toList();
    }

    @GetMapping("/active/user/{userId}")
    public List<LoanDto> getUserActiveLoans(@PathVariable UUID userId) {
        return loanService.getActiveLoansByUser(userId).stream().map(this::toDto).toList();
    }

    @GetMapping("/statistics/popular-books")
    public List<PopularBookDto> getPopularBooks(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year
    ) {
        return loanService.getMostPopularBooks(limit, year);
    }

    @GetMapping("/statistics/summary")
    public Map<String, Object> getSummaryStatistics() {
        return loanService.getSummaryStatistics();
    }

    private LoanDto toDto(Loan loan) {
        return new LoanDto(
                loan.getId(),
                loan.getUser() != null ? loan.getUser().getId() : null,
                loan.getUser() != null ? loan.getUser().getFullName() : null,
                loan.getCopy() != null ? loan.getCopy().getId() : null,
                loan.getCopy() != null ? loan.getCopy().getInventoryNumber() : null,
                loan.getCopy() != null && loan.getCopy().getItem() != null ? loan.getCopy().getItem().getTitle() : null,
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus()
        );
    }
}