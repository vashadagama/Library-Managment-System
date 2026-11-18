package com.example.lims.service;

import com.example.lims.dto.LoanDto;
import com.example.lims.entity.item.Book;
import com.example.lims.entity.item.ItemCopy;
import com.example.lims.entity.loan.Fine;
import com.example.lims.entity.loan.Loan;
import com.example.lims.entity.status.ItemStatus;
import com.example.lims.entity.status.LoanStatus;
import com.example.lims.entity.status.UserStatus;
import com.example.lims.entity.user.User;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.repository.FineRepository;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.ItemStatusRepository;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.LoanStatusRepository;
import com.example.lims.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemCopyRepository itemCopyRepository;
    @Mock private ItemStatusRepository itemStatusRepository;
    @Mock private LoanStatusRepository loanStatusRepository;
    @Mock private FineRepository fineRepository;
    @Mock private SystemParameterService systemParameterService;
    @Mock private AuditService auditService;

    @InjectMocks private LoanService loanService;

    private static ItemStatus itemStatus(String name) {
        ItemStatus status = new ItemStatus();
        status.setId(UUID.randomUUID());
        status.setName(name);
        return status;
    }

    private static LoanStatus loanStatus(String name) {
        LoanStatus status = new LoanStatus();
        status.setId(UUID.randomUUID());
        status.setName(name);
        return status;
    }

    private static User activeUser(int maxLoans) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        UserStatus status = new UserStatus();
        status.setName("ACTIVE");
        user.setStatus(status);
        user.setMaxActiveLoans(maxLoans);
        return user;
    }

    private static ItemCopy copy(String statusName) {
        ItemCopy copy = new ItemCopy();
        copy.setId(UUID.randomUUID());
        copy.setInventoryNumber("INV-1");
        copy.setStatus(itemStatus(statusName));
        Book book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle("Книга");
        copy.setItem(book);
        return copy;
    }

    @Test
    void issueBookByCopyCreatesActiveLoanAndMarksCopyBorrowed() {
        User user = activeUser(5);
        ItemCopy copy = copy("AVAILABLE");
        LoanStatus active = loanStatus("ACTIVE");
        ItemStatus borrowed = itemStatus("BORROWED");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemCopyRepository.findByIdForUpdate(copy.getId())).thenReturn(Optional.of(copy));
        when(loanRepository.countByUserIdAndStatusNames(eq(user.getId()), any())).thenReturn(0L);
        when(itemStatusRepository.findByNameIgnoreCase("BORROWED")).thenReturn(Optional.of(borrowed));
        when(itemCopyRepository.save(copy)).thenReturn(copy);
        when(systemParameterService.getInt("loan_period_days", 14)).thenReturn(14);
        when(loanStatusRepository.findByNameIgnoreCase("ACTIVE")).thenReturn(Optional.of(active));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanDto result = loanService.issueBookByCopy(user.getId(), copy.getId());

        assertEquals("ACTIVE", result.getStatusName());
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());
        assertEquals("BORROWED", copy.getStatus().getName());
        verify(itemCopyRepository).save(copy);
    }

    @Test
    void issueBookByCopyRejectsUnavailableCopy() {
        User user = activeUser(5);
        ItemCopy copy = copy("BORROWED");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemCopyRepository.findByIdForUpdate(copy.getId())).thenReturn(Optional.of(copy));

        assertThrows(BusinessLogicException.class,
                () -> loanService.issueBookByCopy(user.getId(), copy.getId()));
    }

    @Test
    void returnBookCreatesFineForOverdueLoan() {
        User user = activeUser(5);
        ItemCopy copy = copy("BORROWED");
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setUser(user);
        loan.setCopy(copy);
        loan.setDueDate(LocalDate.now().minusDays(2));
        loan.setStatus(loanStatus("ACTIVE"));

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanStatusRepository.findByNameIgnoreCase("RETURNED")).thenReturn(Optional.of(loanStatus("RETURNED")));
        when(itemStatusRepository.findByNameIgnoreCase("AVAILABLE")).thenReturn(Optional.of(itemStatus("AVAILABLE")));
        when(systemParameterService.getDecimal("fine_per_day", BigDecimal.TEN)).thenReturn(new BigDecimal("10.00"));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(loan)).thenReturn(loan);

        LoanDto result = loanService.returnBook(loan.getId());

        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(fineCaptor.capture());
        assertEquals(new BigDecimal("20.00"), fineCaptor.getValue().getAmount());
        assertEquals("RETURNED", result.getStatusName());
        assertEquals("AVAILABLE", copy.getStatus().getName());
    }

    @Test
    void renewLoanExtendsDueDateAndIncrementsCounter() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setDueDate(LocalDate.now());
        loan.setRenewalCount(0);
        loan.setStatus(loanStatus("ACTIVE"));

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanStatusRepository.findByNameIgnoreCase("RENEWED")).thenReturn(Optional.of(loanStatus("RENEWED")));
        when(loanRepository.save(loan)).thenReturn(loan);

        LoanDto result = loanService.renewLoan(loan.getId(), 3);

        assertEquals(LocalDate.now().plusDays(3), result.getDueDate());
        assertEquals(1, loan.getRenewalCount());
        assertEquals("RENEWED", result.getStatusName());
    }

    @Test
    void getActiveLoansByUserReturnsMappedLoans() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(5);
        user.setId(userId);
        ItemCopy copy = copy("BORROWED");
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setUser(user);
        loan.setCopy(copy);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(5));
        loan.setStatus(loanStatus("ACTIVE"));

        when(loanRepository.findByUserIdAndStatusNames(eq(userId), any())).thenReturn(List.of(loan));

        List<LoanDto> result = loanService.getActiveLoansByUser(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.getFirst().getUserId());
        assertTrue(result.getFirst().getDueDate().isAfter(LocalDate.now()));
    }
}
