package main.java.com.vasha.lims.model;

import main.java.com.vasha.lims.enums.LoanStatus;
import static main.java.com.vasha.lims.util.ValidationUtil.*;
import java.time.LocalDate;
import java.util.UUID;

public class Loan {
    private UUID id;
    private User user;
    private LibraryItem item;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    public Loan(User user, LibraryItem item) {
        setUser(user);
        setItem(item);

        if (item.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("Нет доступных для выдачи экземпляров " + item);
        }

        item.decrementAvailableCopies();

        this.id = UUID.randomUUID();
        this.loanDate = LocalDate.now();
        this.dueDate = loanDate.plusDays(14);
        this.status = LoanStatus.ACTIVE;
    }

    public Loan() {}



    public LoanStatus getStatus(){
        return status;
    }

    public User getUser() {
        return user;
    }

    public final void setUser(User user) {
        checkNotNull(user, "Читатель");
        this.user = user;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LibraryItem getItem() {
        return item;
    }

    public final void setItem(LibraryItem item) {
        checkNotNull(item, "Экземпляр");
        this.item = item;
    }

    public final void returnItem() {
        if (this.status == LoanStatus.RETURNED) return;

        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
        item.incrementAvailableCopies();
    }

    public final void renewLoan(Integer extraDays) {
        checkIsPositive(extraDays, "Дополнительные дни");

        if (this.status == LoanStatus.ACTIVE) {
            this.dueDate = this.dueDate.plusDays(extraDays);
            this.status = LoanStatus.RENEWED;

        } else if (this.status == LoanStatus.RENEWED ||
                   this.status == LoanStatus.OVERDUE ||
                   this.status == LoanStatus.LOST) {

            throw new IllegalStateException("Нельзя продлить книгу со статусом: " + status);
        }
    }

    public final void updateStatusIfOverdue() {
        if (this.status != LoanStatus.RETURNED &&
            this.status != LoanStatus.LOST &&
            LocalDate.now().isAfter(this.dueDate)) {

            this.status = LoanStatus.OVERDUE;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Выдача [%s]: %s взял '%s'. Вернуть до: %s (Статус: %s)",
                id.toString().substring(0, 8),
                user.getFullName(),
                item.getTitle(),
                dueDate,
                status.getDisplayName()
        );
    }
}
