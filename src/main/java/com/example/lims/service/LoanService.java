package com.example.lims.service;

import com.example.lims.dto.PopularBookDto;
import com.example.lims.enums.ItemStatus;
import com.example.lims.enums.LoanStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.ItemCopy;
import com.example.lims.model.Loan;
import com.example.lims.model.User;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ItemCopyRepository itemCopyRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository, ItemCopyRepository itemCopyRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.itemCopyRepository = itemCopyRepository;
    }

    @Transactional
    public Loan issueBookByLibraryItem(UUID userId, UUID libraryItemId) {
        User user = validateUserForLoan(userId);

        ItemCopy availableCopy = itemCopyRepository.findByItemIdAndStatus(libraryItemId, ItemStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Нет свободных экземпляров этого издания"));

        return createLoan(user, availableCopy);
    }

    @Transactional
    public Loan issueBookByCopy(UUID userId, UUID copyId) {
        User user = validateUserForLoan(userId);

        ItemCopy copy = itemCopyRepository.findById(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Экземпляр не найден"));

        if (copy.getStatus() != ItemStatus.AVAILABLE) {
            throw new BusinessLogicException("Экземпляр недоступен для выдачи. Текущий статус: " + copy.getStatus().getDisplayName());
        }

        return createLoan(user, copy);
    }

    private User validateUserForLoan(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (user.getStatus() == com.example.lims.enums.UserStatus.BLOCKED ||
                user.getStatus() == com.example.lims.enums.UserStatus.FROZEN) {
            throw new BusinessLogicException("Выдача запрещена. Статус аккаунта: " + user.getStatus().getDisplayName());
        }
        return user;
    }

    private Loan createLoan(User user, ItemCopy copy) {
        long activeLoans = loanRepository.countByUserIdAndStatusIn(user.getId(),
                List.of(LoanStatus.ACTIVE, LoanStatus.RENEWED, LoanStatus.OVERDUE));

        if (activeLoans >= user.getMaxActiveLoans()) {
            throw new BusinessLogicException("Читатель уже взял максимум книг (" + user.getMaxActiveLoans() + " шт)");
        }

        copy.setStatus(ItemStatus.BORROVED);
        itemCopyRepository.save(copy);

        Loan loan = new Loan(user, copy);
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись о выдаче не найдена"));

        loan.returnItem();

        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());
            System.out.printf("Просрочка на %d дней. Штраф (заглушка): %.2f руб.%n", daysOverdue, daysOverdue * 10.0);
        }

        ItemCopy copy = loan.getCopy();
        copy.setStatus(ItemStatus.AVAILABLE);
        itemCopyRepository.save(copy);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan renewLoan(UUID loanId, int extraDays) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись о выдаче не найдена"));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.RENEWED) {
            throw new BusinessLogicException("Нельзя продлить выдачу со статусом: " + loan.getStatus().getDisplayName());
        }

        loan.renewLoan(extraDays);
        return loanRepository.save(loan);
    }

    public List<Loan> getOverdueLoans() {
        return loanRepository.findExpiredLoans(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoansByUser(UUID userId) {
        return loanRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoansByCopy(UUID copyId) {
        return loanRepository.findByCopyId(copyId);
    }

    @Transactional(readOnly = true)
    public List<Loan> getActiveLoansByUser(UUID userId) {
        return loanRepository.findByUserIdAndStatusIn(userId,
                List.of(LoanStatus.ACTIVE, LoanStatus.RENEWED, LoanStatus.OVERDUE));
    }

    @Transactional(readOnly = true)
    public List<PopularBookDto> getMostPopularBooks(int limit, Integer year) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> raw = loanRepository.findPopularBooksRaw(year, pageable);

        Map<UUID, PopularBookDto> map = new LinkedHashMap<>();
        for (Object[] row : raw) {
            UUID itemId = (UUID) row[0];
            String title = (String) row[1];
            String lastName = (String) row[2];
            String firstName = (String) row[3];
            long count = ((Number) row[4]).longValue();

            String authorFull = lastName + " " + firstName;
            if (map.containsKey(itemId)) {
                PopularBookDto existing = map.get(itemId);
                String newAuthors = existing.getAuthors() + ", " + authorFull;
                map.put(itemId, new PopularBookDto(itemId, title, newAuthors, count));
            } else {
                map.put(itemId, new PopularBookDto(itemId, title, authorFull, count));
            }
        }
        return new ArrayList<>(map.values());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummaryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLoans", loanRepository.countTotalLoans());
        stats.put("activeLoans", loanRepository.countActiveLoans());
        stats.put("overdueReturns", loanRepository.countOverdueReturns());
        return stats;
    }
}