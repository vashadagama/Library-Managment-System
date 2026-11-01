package com.example.lims.controller;

import com.example.lims.dto.LoanDto;
import com.example.lims.dto.PopularBookDto;
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
        return loanService.issueBookByLibraryItem(userId, libraryItemId);
    }

    @PostMapping("/issue/by-copy")
    public LoanDto issueByCopy(@RequestParam UUID userId, @RequestParam UUID copyId) {
        return loanService.issueBookByCopy(userId, copyId);
    }

    @PostMapping("/return")
    public LoanDto returnBook(@RequestParam UUID loanId) {
        return loanService.returnBook(loanId);
    }

    @GetMapping("/active")
    public List<LoanDto> getAllActive() {
        return loanService.getAllActiveLoans();
    }

    @GetMapping("/overdue")
    public List<LoanDto> getOverdue() {
        return loanService.getOverdueLoans();
    }

    @PostMapping("/{loanId}/renew")
    public LoanDto renewLoan(@PathVariable UUID loanId, @RequestParam(defaultValue = "14") int extraDays) {
        return loanService.renewLoan(loanId, extraDays);
    }

    @GetMapping("/history/user/{userId}")
    public List<LoanDto> getUserHistory(@PathVariable UUID userId) {
        return loanService.getLoansByUser(userId);
    }

    @GetMapping("/history/copy/{copyId}")
    public List<LoanDto> getCopyHistory(@PathVariable UUID copyId) {
        return loanService.getLoansByCopy(copyId);
    }

    @GetMapping("/active/user/{userId}")
    public List<LoanDto> getUserActiveLoans(@PathVariable UUID userId) {
        return loanService.getActiveLoansByUser(userId);
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
}
