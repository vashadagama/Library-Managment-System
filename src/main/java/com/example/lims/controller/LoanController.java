package com.example.lims.controller;

import com.example.lims.model.Loan;
import com.example.lims.service.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/issue")
    public Loan issueBook(@RequestParam UUID userId, @RequestParam UUID itemId) {
        return loanService.issueBook(userId, itemId);
    }

    @PostMapping("/return")
    public Loan returnBook(@RequestParam UUID loanId) {
        return loanService.returnBook(loanId);
    }

    @GetMapping("/overdue")
    public List<Loan> getOverdue() {
        return loanService.getOverdueLoans();
    }
}