package com.example.lims.service;

import com.example.lims.enums.ItemStatus;
import com.example.lims.enums.LoanStatus;
import com.example.lims.model.ItemCopy;
import com.example.lims.model.Loan;
import com.example.lims.model.User;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
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
    public Loan issueBook(UUID userId, UUID itemId) {
    
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        ItemCopy availableCopy = itemCopyRepository.findByItemIdAndStatus(itemId, ItemStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Нет свободных экземпляров этой книги"));

        long activeLoans = loanRepository.findAll().stream()
                .filter(l -> l.getUser().getId().equals(userId) && l.getStatus() == LoanStatus.ACTIVE)
                .count();
    
        if (activeLoans >= 3) {
            throw new RuntimeException("Читатель уже взял максимум книг (3 шт)");
        }

        availableCopy.setStatus(ItemStatus.BORROVED);
        itemCopyRepository.save(availableCopy);

        Loan loan = new Loan(user, availableCopy);
        loan.setDueDate(java.time.LocalDate.now().plusDays(14));
    
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Запись о выдаче не найдена"));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("Эта книга уже возвращена или утеряна");
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(java.time.LocalDate.now());


        ItemCopy copy = loan.getCopy();
        copy.setStatus(ItemStatus.AVAILABLE);
        itemCopyRepository.save(copy);

        return loanRepository.save(loan);
    }


    public List<Loan> getOverdueLoans() {
        return loanRepository.findExpiredLoans(java.time.LocalDate.now());
    }
}