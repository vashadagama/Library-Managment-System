package main.java.com.vasha.lims.service;

import java.util.UUID;
import main.java.com.vasha.lims.enums.LoanStatus;
import main.java.com.vasha.lims.model.LibraryItem;
import main.java.com.vasha.lims.model.Loan;
import main.java.com.vasha.lims.model.User;
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
        User user = userRepo.findById(userId);
        LibraryItem item = itemRepo.findById(itemId);
        
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (item == null) {
            throw new IllegalArgumentException("Экземпляр не найден");
        }

        if (item.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("Нет доступных копий экземпляра " + item.getTitle());
        }
        for (Loan loan : loanRepo.findLoansByUser(user)) {
            if (loan.getItem().getId().equals(itemId) && 
            (loan.getStatus() == LoanStatus.ACTIVE || 
             loan.getStatus() == LoanStatus.RENEWED)) {
                
                throw new IllegalArgumentException("Пользователь уже взял экземпляр этой книги");
            }
            if (loan.getStatus() == LoanStatus.OVERDUE) {
                throw new IllegalArgumentException("Пользователь просрочил возврат книги " + loan.getItem().getTitle());
            }
        }

        Loan loan = new Loan(user, item);
        loanRepo.save(loan);
        return loan;
    }

    
    
    
    //доработать 3. безопасность далее по тз.
    public final void issueLoan(UUID userId, UUID itemId) {}

    public final void returnItem(UUID userId, UUID itemId) {
        User user = userRepo.findById(userId);
        
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("Экземпляр не найден");
        }

        for (Loan loan : loanRepo.findLoansByUser(user)){
            if (loan.getItem().getId().equals(itemId) && 
            loan.getStatus() == LoanStatus.ACTIVE) {

                loan.returnItem();
            }
        }
    }



}
