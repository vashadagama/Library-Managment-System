package com.example.lims.service;

import com.example.lims.dto.LoanDto;
import com.example.lims.dto.PopularBookDto;
import com.example.lims.entity.item.ItemCopy;
import com.example.lims.entity.item.LibraryItem;
import com.example.lims.entity.loan.Fine;
import com.example.lims.entity.loan.Loan;
import com.example.lims.entity.status.ItemStatus;
import com.example.lims.entity.status.LoanStatus;
import com.example.lims.entity.user.User;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Transactional
public class LoanService {

    private static final String ACTIVE = "ACTIVE";
    private static final String RETURNED = "RETURNED";
    private static final String RENEWED = "RENEWED";
    private static final String OVERDUE = "OVERDUE";
    private static final String AVAILABLE = "AVAILABLE";
    private static final String BORROWED = "BORROWED";

    private static final List<String> ACTIVE_STATUSES = List.of(ACTIVE, RENEWED, OVERDUE);

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ItemCopyRepository itemCopyRepository;
    private final ItemStatusRepository itemStatusRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final FineRepository fineRepository;
    private final SystemParameterService systemParameterService;

    public LoanService(LoanRepository loanRepository,
                       UserRepository userRepository,
                       ItemCopyRepository itemCopyRepository,
                       ItemStatusRepository itemStatusRepository,
                       LoanStatusRepository loanStatusRepository,
                       FineRepository fineRepository,
                       SystemParameterService systemParameterService) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.itemCopyRepository = itemCopyRepository;
        this.itemStatusRepository = itemStatusRepository;
        this.loanStatusRepository = loanStatusRepository;
        this.fineRepository = fineRepository;
        this.systemParameterService = systemParameterService;
    }

    public LoanDto issueBookByLibraryItem(UUID userId, UUID libraryItemId) {
        User user = validateUserForLoan(userId);

        ItemCopy availableCopy = itemCopyRepository
                .findForUpdateByItemIdAndStatusName(libraryItemId, AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Нет свободных экземпляров этого издания"));

        return toDto(createLoan(user, availableCopy));
    }

    public LoanDto issueBookByCopy(UUID userId, UUID copyId) {
        User user = validateUserForLoan(userId);

        ItemCopy copy = itemCopyRepository.findByIdForUpdate(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Экземпляр не найден"));

        if (!AVAILABLE.equalsIgnoreCase(copy.getStatus().getName())) {
            throw new BusinessLogicException("Экземпляр недоступен для выдачи. Текущий статус: " + copy.getStatus().getName());
        }

        return toDto(createLoan(user, copy));
    }

    public LoanDto returnBook(UUID loanId) {
        Loan loan = getEntity(loanId);

        if (RETURNED.equalsIgnoreCase(loan.getStatus().getName())) {
            throw new BusinessLogicException("Эта выдача уже возвращена");
        }

        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);
        loan.setStatus(loanStatus(RETURNED));

        ItemCopy copy = loan.getCopy();
        copy.setStatus(itemStatus(AVAILABLE));
        itemCopyRepository.save(copy);

        if (returnDate.isAfter(loan.getDueDate())) {
            long daysOverdue = DAYS.between(loan.getDueDate(), returnDate);
            if (daysOverdue > 0) {
                BigDecimal rate = systemParameterService.getDecimal("fine_per_day", BigDecimal.TEN);
                Fine fine = new Fine();
                fine.setAmount(rate.multiply(BigDecimal.valueOf(daysOverdue)));
                fine.setReason("Просрочка на " + daysOverdue + " дн.");
                fine.setIsPaid(false);
                fine.setLoan(loan);
                fineRepository.save(fine);
            }
        }

        return toDto(loanRepository.save(loan));
    }

    public LoanDto renewLoan(UUID loanId, int extraDays) {
        if (extraDays <= 0) {
            throw new BusinessLogicException("Количество дней продления должно быть положительным");
        }

        Loan loan = getEntity(loanId);
        String status = loan.getStatus().getName();

        if (!ACTIVE.equalsIgnoreCase(status) && !RENEWED.equalsIgnoreCase(status)) {
            throw new BusinessLogicException("Нельзя продлить выдачу со статусом: " + status);
        }

        loan.setDueDate(loan.getDueDate().plusDays(extraDays));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        loan.setStatus(loanStatus(RENEWED));

        return toDto(loanRepository.save(loan));
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getAllActiveLoans() {
        return loanRepository.findByStatusNames(ACTIVE_STATUSES).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getOverdueLoans() {
        return loanRepository.findExpiredLoans(ACTIVE_STATUSES).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getLoansByUser(UUID userId) {
        return loanRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getLoansByCopy(UUID copyId) {
        return loanRepository.findByCopyId(copyId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getActiveLoansByUser(UUID userId) {
        return loanRepository.findByUserIdAndStatusNames(userId, ACTIVE_STATUSES)
                .stream().map(this::toDto).toList();
    }

    public int markOverdueLoans() {
        LoanStatus overdueStatus = loanStatus(OVERDUE);
        List<Loan> expiredLoans = loanRepository.findExpiredLoans(ACTIVE_STATUSES);
        int changed = 0;
        for (Loan loan : expiredLoans) {
            if (!OVERDUE.equalsIgnoreCase(loan.getStatus().getName())) {
                loan.setStatus(overdueStatus);
                changed++;
            }
        }
        if (changed > 0) {
            loanRepository.saveAll(expiredLoans);
        }
        return changed;
    }

    @Transactional(readOnly = true)
    public List<PopularBookDto> getMostPopularBooks(int limit, Integer year) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> raw = loanRepository.findPopularBooksRaw(year, pageable);
        return raw.stream()
                .map(row -> new PopularBookDto(
                        (UUID) row[0],
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummaryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLoans", loanRepository.countTotalLoans());
        stats.put("activeLoans", loanRepository.countActiveLoans());
        stats.put("overdueReturns", loanRepository.countOverdueReturns());
        return stats;
    }

    private User validateUserForLoan(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (!ACTIVE.equalsIgnoreCase(user.getStatus().getName())) {
            throw new BusinessLogicException("Выдача запрещена. Статус аккаунта: " + user.getStatus().getName());
        }
        return user;
    }

    private Loan createLoan(User user, ItemCopy copy) {
        long activeLoans = loanRepository.countByUserIdAndStatusNames(user.getId(), ACTIVE_STATUSES);
        int limit = user.getMaxActiveLoans() != null
                ? user.getMaxActiveLoans()
                : systemParameterService.getInt("max_active_loans_default", 5);

        if (activeLoans >= limit) {
            throw new BusinessLogicException("Читатель уже взял максимум книг (" + limit + " шт)");
        }

        copy.setStatus(itemStatus(BORROWED));
        itemCopyRepository.save(copy);

        int loanPeriodDays = systemParameterService.getInt("loan_period_days", 14);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setCopy(copy);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(loanPeriodDays));
        loan.setRenewalCount(0);
        loan.setStatus(loanStatus(ACTIVE));

        return loanRepository.save(loan);
    }

    private Loan getEntity(UUID id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись о выдаче не найдена"));
    }

    private LoanStatus loanStatus(String name) {
        return loanStatusRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Статус выдачи не найден: " + name));
    }

    private ItemStatus itemStatus(String name) {
        return itemStatusRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Статус экземпляра не найден: " + name));
    }

    private LoanDto toDto(Loan loan) {
        ItemCopy copy = loan.getCopy();
        LibraryItem item = copy != null ? copy.getItem() : null;
        return new LoanDto(
                loan.getId(),
                loan.getUser() != null ? loan.getUser().getId() : null,
                loan.getUser() != null ? loan.getUser().getFullName() : null,
                copy != null ? copy.getId() : null,
                copy != null ? copy.getInventoryNumber() : null,
                item != null ? item.getTitle() : null,
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus() != null ? loan.getStatus().getId() : null,
                loan.getStatus() != null ? loan.getStatus().getName() : null
        );
    }
}
