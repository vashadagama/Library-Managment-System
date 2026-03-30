package main.java.com.vasha.lims.service;

import java.util.UUID;
import main.java.com.vasha.lims.enums.LoanStatus;
import main.java.com.vasha.lims.model.LibraryItem;
import main.java.com.vasha.lims.model.Loan;
import main.java.com.vasha.lims.model.User;
import static main.java.com.vasha.lims.util.ValidationUtil.*;
import main.java.com.vasha.lims.repository.ItemRepository;
import main.java.com.vasha.lims.repository.LoanRepository;
import main.java.com.vasha.lims.repository.UserRepository;

public class LibraryService {
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final LoanRepository loanRepo;
    
    public LibraryService(UserRepository userRepo, ItemRepository itemRepo, LoanRepository loanRepo) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.loanRepo = loanRepo;
    }



    public final Loan borrowItem(UUID userId, UUID itemId) {
        checkNotNull(userId, "Id пользователя");
        checkNotNull(itemId, "Id экземпляра");

        User user = userRepo.findById(userId);
        checkNotNull(user, "Пользователь");
        LibraryItem item = itemRepo.findById(itemId);
        checkNotNull(item, "Экземпляр");

        if (item.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Нет доступных копий экземпляра " + item.getTitle());
        }

        for (Loan loan : loanRepo.findLoansByUser(user)) {
            if (loan.getStatus() == LoanStatus.OVERDUE) {
                throw new IllegalStateException("Пользователь не вернул книгу " + loan.getItem().getTitle());
            }

            if (loan.getItem().getId().equals(itemId) && 
            (loan.getStatus() == LoanStatus.ACTIVE || 
             loan.getStatus() == LoanStatus.RENEWED)) {
                
                throw new IllegalStateException("Пользователь уже взял экземпляр этой книги");
            }

        }

        Loan loan = new Loan(user, item);
        loanRepo.save(loan);
        return loan;
    }

    public final void returnItem(UUID userId, UUID itemId) {
        checkNotNull(userId, "Id пользователя");
        User user = userRepo.findById(userId);
        checkNotNull(user, "Пользователь");
        checkNotNull(itemId, "Id экземпляра");

        for (Loan loan : loanRepo.findLoansByUser(user)){
            if (loan.getItem().getId().equals(itemId) &&
                    (loan.getStatus() == LoanStatus.ACTIVE ||
                     loan.getStatus() == LoanStatus.RENEWED ||
                     loan.getStatus() == LoanStatus.OVERDUE)) {

                loan.returnItem();
                return;
            }
        }
        throw new IllegalStateException("Выдача не найдена");
    }
}
