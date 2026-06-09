package com.example.lims.model;

import com.example.lims.enums.LoanStatus;
import com.example.lims.enums.ItemStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    @Test
    void testDefaultConstructor() {
        Loan loan = new Loan();
        assertNotNull(loan);
        assertNotNull(loan.getLoanDate());
        assertNotNull(loan.getDueDate());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertNull(loan.getReturnDate());
    }

    @Test
    void testParameterizedConstructor() {
        User user = new User();
        ItemCopy copy = new ItemCopy();
        Loan loan = new Loan(user, copy);
        assertEquals(user, loan.getUser());
        assertEquals(copy, loan.getCopy());
        assertEquals(ItemStatus.BORROVED, copy.getStatus());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    void testReturnItem() {
        User user = new User();
        ItemCopy copy = new ItemCopy();
        Loan loan = new Loan(user, copy);
        loan.returnItem();
        assertNotNull(loan.getReturnDate());
        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(ItemStatus.AVAILABLE, copy.getStatus());
    }

    @Test
    void testReturnItemAlreadyReturned() {
        User user = new User();
        ItemCopy copy = new ItemCopy();
        Loan loan = new Loan(user, copy);
        loan.returnItem();
        assertThrows(IllegalStateException.class, loan::returnItem);
    }

    @Test
    void testRenewLoan() {
        User user = new User();
        ItemCopy copy = new ItemCopy();
        Loan loan = new Loan(user, copy);
        LocalDate originalDue = loan.getDueDate();
        loan.renewLoan(7);
        assertEquals(originalDue.plusDays(7), loan.getDueDate());
        assertEquals(LoanStatus.RENEWED, loan.getStatus());
    }

    @Test
    void testRenewLoanInvalidStatus() {
        User user = new User();
        ItemCopy copy = new ItemCopy();
        Loan loan = new Loan(user, copy);
        loan.returnItem();
        assertThrows(IllegalStateException.class, () -> loan.renewLoan(5));
    }

    @Test
    void testSettersAndGetters() {
        Loan loan = new Loan();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        loan.setId(id);
        loan.setLoanDate(date);
        loan.setDueDate(date.plusDays(10));
        loan.setReturnDate(date.plusDays(5));
        loan.setStatus(LoanStatus.RENEWED);

        assertEquals(id, loan.getId());
        assertEquals(date, loan.getLoanDate());
        assertEquals(date.plusDays(10), loan.getDueDate());
        assertEquals(date.plusDays(5), loan.getReturnDate());
        assertEquals(LoanStatus.RENEWED, loan.getStatus());
    }
}