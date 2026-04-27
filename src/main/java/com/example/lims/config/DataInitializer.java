package com.example.lims.config;

import com.example.lims.enums.*;
import com.example.lims.model.*;
import com.example.lims.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final MagazineRepository magazineRepository;
    private final ItemCopyRepository itemCopyRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AuthorRepository authorRepository, BookRepository bookRepository,
                           MagazineRepository magazineRepository, ItemCopyRepository itemCopyRepository,
                           UserRepository userRepository, LoanRepository loanRepository,
                           PasswordEncoder passwordEncoder) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.magazineRepository = magazineRepository;
        this.itemCopyRepository = itemCopyRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            System.out.println("База данных уже содержит пользователей. Пропускаем инициализацию.");
            return;
        }

        System.out.println("Начинаем заполнение базы тестовыми данными...");

        Author author1 = new Author("Джоан", "Роулинг", "Кэтлин");
        Author author2 = new Author("Стивен", "Кинг", "Эдвин");
        authorRepository.save(author1);
        authorRepository.save(author2);

        Book book1 = new Book("Гарри Поттер и Философский камень", "Росмэн",
                LocalDate.of(1997, 6, 26), "978-5-353-00308-3",
                "Стеллаж 1, Полка А", "Русский", BookGenre.CHILDREN, 399);
        book1.addAuthor(author1);
        bookRepository.save(book1);

        Book book2 = new Book("Зеленая миля", "АСТ",
                LocalDate.of(1996, 8, 29), "978-5-17-080115-2",
                "Стеллаж 3, Полка Б", "Русский", BookGenre.FICTION, 480);
        book2.addAuthor(author2);
        bookRepository.save(book2);

        Magazine mag1 = new Magazine("National Geographic", "NatGeo Society",
                LocalDate.of(2023, 10, 1), "1234-5678",
                "Стенд журналов", "Английский", MagazineGenre.POPULAR_SCIENTIFIC, 120, true);
        magazineRepository.save(mag1);

        ItemCopy copy1Book1 = new ItemCopy(book1, "INV-BK-001");
        ItemCopy copy2Book1 = new ItemCopy(book1, "INV-BK-002");
        ItemCopy copy1Book2 = new ItemCopy(book2, "INV-BK-003");
        ItemCopy copy1Mag1 = new ItemCopy(mag1, "INV-MG-001");

        itemCopyRepository.save(copy1Book1);
        itemCopyRepository.save(copy2Book1);
        itemCopyRepository.save(copy1Book2);
        itemCopyRepository.save(copy1Mag1);

        User admin = new User("Иван", "Иванов", "Иванович", "admin@lims.com",
                UserRole.ADMIN, passwordEncoder.encode("VCa9m15nDs4"), "LIB-0001");

        User librarian = new User("Петр", "Петров", "Сергеевич", "petr@test.com",
                UserRole.LIBRARIAN, passwordEncoder.encode("aBamaBlack17"), "LIB-0002");

        User reader1 = User.createReader("Антонина", "Улицкая", "Ивановна",
                "antonina@test.com", "LIB-0003");

        User reader2 = User.createReader("Сергей", "Сергеев", "Петрович",
                "sergey@test.com", "LIB-0004");

        userRepository.save(admin);
        userRepository.save(librarian);
        userRepository.save(reader1);
        userRepository.save(reader2);

        Loan loan1 = new Loan(reader1, copy1Book1);
        loanRepository.save(loan1);

        System.out.println("Тестовые данные успешно загружены!");
    }
}