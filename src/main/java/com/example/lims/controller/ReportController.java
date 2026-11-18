package com.example.lims.controller;

import com.example.lims.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Отчёты по ТЗ с выгрузкой в PDF.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final String PDF_TYPE = "application/pdf";

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/popular-books")
    public ResponseEntity<byte[]> popularBooks(@RequestParam(required = false) Integer year) {
        return pdf(reportService.popularBooksPdf(year), "popular-books.pdf");
    }

    @GetMapping("/active-readers")
    public ResponseEntity<byte[]> activeReaders() {
        return pdf(reportService.activeReadersPdf(), "active-readers.pdf");
    }

    @GetMapping("/fine-debts")
    public ResponseEntity<byte[]> fineDebts() {
        return pdf(reportService.fineDebtsPdf(), "fine-debts.pdf");
    }

    @GetMapping("/overdue-loans")
    public ResponseEntity<byte[]> overdueLoans() {
        return pdf(reportService.overdueLoansPdf(), "overdue-loans.pdf");
    }

    private ResponseEntity<byte[]> pdf(byte[] content, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(PDF_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(content);
    }
}
