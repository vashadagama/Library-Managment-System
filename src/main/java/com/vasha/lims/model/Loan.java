package main.java.com.vasha.lims.model;

import main.java.com.vasha.lims.enums.LoanStatus;
import static main.java.com.vasha.lims.util.ValidationUtil.*;
import java.time.LocalDate;
import java.util.UUID;

public class Loan {
    private UUID id;
    private User user;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private ItemCopy copy;

    public Loan(User user, ItemCopy copy) {
        setUser(user);
        setCopy(copy);

        this.id = UUID.randomUUID();
        this.loanDate = LocalDate.now();
        this.dueDate = loanDate.plusDays(14);
        this.status = LoanStatus.ACTIVE;
    }

    public Loan() {}

//    Вместо этого меняй статус самого экземпляра: copy.setStatus(ItemStatus.LOANED);.
//    В методе returnItem() в классе Loan делай то же самое при возврате: copy.setStatus(ItemStatus.AVAILABLE);.
//    В LoanRepository:
//    Везде, где ты искал совпадения по item, теперь нужно достучаться до item через copy. Например: loan.getCopy().getItem().equals(item).


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

    public ItemCopy getCopy() {
        return copy;
    }

    public final void setCopy(ItemCopy copy) {
        checkNotNull(copy, "Экземпляр");
        this.copy = copy;
    }

    public final void returnItem() {
        if (this.status == LoanStatus.RETURNED) return;

        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
        copy.incrementAvailableCopies();
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
                copy.getTitle(),
                dueDate,
                status.getDisplayName()
        );
    }
}
