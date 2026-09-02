package com.example.lims.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemCopyRepository itemCopyRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    void returnBook() {
        UUID loanId = UUID.randomUUID();
        User user = new User();
        ItemCopy copy = new ItemCopy();
        copy.setStatus(ItemStatus.BORROVED);
        Loan loan = new Loan(user, copy);
        loan.setDueDate(LocalDate.now().minusDays(1));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));
        when(itemCopyRepository.save(any(ItemCopy.class))).thenAnswer(i -> i.getArgument(0));

        Loan result = loanService.returnBook(loanId);

        assertNotNull(result);
        assertEquals(LoanStatus.RETURNED, result.getStatus());
        assertEquals(ItemStatus.AVAILABLE, result.getCopy().getStatus());
        verify(itemCopyRepository).save(any(ItemCopy.class));
    }

    @Test
    void getLoansByUser() {
        UUID userId = UUID.randomUUID();
        when(loanRepository.findByUserId(userId)).thenReturn(List.of(new Loan()));

        List<Loan> result = loanService.getLoansByUser(userId);

        assertFalse(result.isEmpty());
        verify(loanRepository).findByUserId(userId);
    }
}