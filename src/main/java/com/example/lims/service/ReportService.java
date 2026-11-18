package com.example.lims.service;

import com.example.lims.entity.loan.Loan;
import com.example.lims.repository.LoanRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Формирование отчётов по ТЗ в формате PDF:
 * «Популярные издания», «Активные читатели», «Задолженности по штрафам»,
 * «Просроченные выдачи».
 */
@Slf4j
@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final LoanRepository loanRepository;
    private final FineService fineService;
    private final LoanService loanService;
    private final SystemParameterService systemParameterService;

    public ReportService(LoanRepository loanRepository,
                         FineService fineService,
                         LoanService loanService,
                         SystemParameterService systemParameterService) {
        this.loanRepository = loanRepository;
        this.fineService = fineService;
        this.loanService = loanService;
        this.systemParameterService = systemParameterService;
    }

    @Transactional(readOnly = true)
    public byte[] popularBooksPdf(Integer year) {
        PdfPTable table = new PdfPTable(new float[]{0.8f, 5.5f, 4.2f});
        addHeaderRow(table, "Выдач", "Издание", "Авторы");

        loanService.getMostPopularBooks(50, year).forEach(book -> {
            table.addCell(String.valueOf(book.getLoanCount()));
            table.addCell(nullToDash(book.getTitle()));
            table.addCell(nullToDash(book.getAuthors()));
        });

        String subtitle = year != null ? "Период: " + year + " год" : "Период: за всё время";
        return buildPdf("Отчёт «Популярные издания»", subtitle, table);
    }

    @Transactional(readOnly = true)
    public byte[] activeReadersPdf() {
        List<Object[]> raw = loanRepository.findActiveReadersRaw();

        PdfPTable table = new PdfPTable(new float[]{0.8f, 4.5f, 3.5f, 2.5f, 1.2f});
        addHeaderRow(table, "№", "Читатель", "Email", "Чит. билет", "Выдач");
        int index = 1;
        for (Object[] row : raw) {
            table.addCell(String.valueOf(index++));
            table.addCell(nullToDash((String) row[1]));
            table.addCell(nullToDash((String) row[2]));
            table.addCell(nullToDash((String) row[3]));
            table.addCell(String.valueOf(((Number) row[4]).longValue()));
        }

        return buildPdf("Отчёт «Активные читатели»",
                "Читатели с незавершёнными выдачами, по убыванию активности", table);
    }

    @Transactional(readOnly = true)
    public byte[] fineDebtsPdf() {
        var fines = fineService.getFines(false);

        PdfPTable table = new PdfPTable(new float[]{3.8f, 4.2f, 2.2f, 1.8f, 1.8f});
        addHeaderRow(table, "Читатель", "Издание", "Причина", "Срок", "Сумма, руб.");
        BigDecimal total = BigDecimal.ZERO;
        for (var fine : fines) {
            total = total.add(fine.getAmount() != null ? fine.getAmount() : BigDecimal.ZERO);
            table.addCell(nullToDash(fine.getUserFullName()));
            table.addCell(nullToDash(fine.getBookTitle()));
            table.addCell(nullToDash(fine.getReason()));
            table.addCell(fine.getDueDate() != null ? fine.getDueDate().format(DATE_FMT) : "—");
            table.addCell(fine.getAmount() != null ? fine.getAmount().toPlainString() : "—");
        }

        return buildPdf("Отчёт «Задолженности по штрафам»",
                "Неоплаченные штрафы. Итого: " + total.toPlainString() + " руб.", table);
    }

    @Transactional(readOnly = true)
    public byte[] overdueLoansPdf() {
        List<LoanDtoAdapter> overdue = loanService.getOverdueLoans().stream()
                .map(dto -> new LoanDtoAdapter(dto.getUserFullName(), dto.getBookTitle(),
                        dto.getInventoryNumber(), dto.getDueDate(), dto.getFineAmount()))
                .toList();

        PdfPTable table = new PdfPTable(new float[]{3.8f, 4.2f, 2.4f, 1.8f, 1.8f});
        addHeaderRow(table, "Читатель", "Издание", "Инв. номер", "Срок", "Штраф, руб.");
        for (var loan : overdue) {
            table.addCell(nullToDash(loan.userFullName()));
            table.addCell(nullToDash(loan.bookTitle()));
            table.addCell(nullToDash(loan.inventoryNumber()));
            table.addCell(loan.dueDate() != null ? loan.dueDate().format(DATE_FMT) : "—");
            table.addCell(loan.fineAmount() != null ? loan.fineAmount().toPlainString() : "—");
        }

        return buildPdf("Отчёт «Просроченные выдачи»",
                "Выдачи с истёкшим сроком возврата. Штраф — проекция по ставке за день.", table);
    }

    private byte[] buildPdf(String title, String subtitle, PdfPTable table) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = font(14, Color.BLACK);
            Font subFont = font(10, Color.DARK_GRAY);
            document.add(new Paragraph("Библиотечная информационная система LIMS", subFont));
            document.add(new Paragraph(title, titleFont));
            if (subtitle != null && !subtitle.isBlank()) {
                Paragraph p = new Paragraph(subtitle, subFont);
                p.setSpacingAfter(8);
                document.add(p);
            }

            table.setWidthPercentage(100);
            table.setSpacingBefore(6);
            document.add(table);

            Paragraph footer = new Paragraph(
                    "Сформировано: " + LocalDateTime.now().format(DATE_TIME_FMT), subFont);
            footer.setSpacingBefore(10);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Ошибка генерации PDF-отчёта «{}»: {}", title, ex.getMessage(), ex);
            throw new IllegalStateException("Не удалось сформировать PDF-отчёт", ex);
        }
    }

    private void addHeaderRow(PdfPTable table, String... headers) {
        Font headerFont = font(10, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(51, 51, 51));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    private Font font(float size, Color color) {
        Font f = new Font(baseFont());
        f.setSize(size);
        f.setColor(color);
        return f;
    }

    /**
     * Кириллица не поддерживается стандартными шрифтами PDF, поэтому
     * подключается системный TTF-шрифт (Windows/Linux), поддерживающий кириллицу.
     */
    private Font baseFont() {
        String[] candidates = {
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/calibri.ttf",
                "C:/Windows/Fonts/times.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/dejavu/DejaVuSans.ttf"
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                return FontFactory.getFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
            }
        }
        log.warn("TTF-шрифт с кириллицей не найден, используется Helvetica (возможны проблемы с кириллицей)");
        return new Font(Font.HELVETICA, 9);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private record LoanDtoAdapter(String userFullName, String bookTitle, String inventoryNumber,
                                  LocalDate dueDate, BigDecimal fineAmount) {
    }
}
