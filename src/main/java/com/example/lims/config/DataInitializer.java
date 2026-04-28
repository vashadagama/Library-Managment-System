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

        // ========== АВТОРЫ (7) ==========
        Author author1 = new Author("Джоан", "Роулинг", "Кэтлин");
        Author author2 = new Author("Стивен", "Кинг", "Эдвин");
        Author author3 = new Author("Джон", "Толкин", "Рональд Руэл");
        Author author4 = new Author("Агата", "Кристи", "Мэри");
        Author author5 = new Author("Джордж", "Оруэлл", "");
        Author author6 = new Author("Рэй", "Брэдбери", "Дуглас");
        Author author7 = new Author("Михаил", "Булгаков", "Афанасьевич");
        authorRepository.saveAll(java.util.List.of(author1, author2, author3, author4, author5, author6, author7));

        // ========== КНИГИ (7) ==========
        Book book1 = new Book("Гарри Поттер и Философский камень", "Росмэн",
                LocalDate.of(1997, 6, 26), "978-5-353-00308-3",
                "Стеллаж 1, Полка А", "Русский", BookGenre.CHILDREN, 399);
        book1.addAuthor(author1);

        Book book2 = new Book("Зеленая миля", "АСТ",
                LocalDate.of(1996, 8, 29), "978-5-17-080115-2",
                "Стеллаж 3, Полка Б", "Русский", BookGenre.FICTION, 480);
        book2.addAuthor(author2);

        Book book3 = new Book("Властелин колец: Братство кольца", "Эксмо",
                LocalDate.of(1954, 7, 29), "978-5-04-093578-8",
                "Стеллаж 2, Полка В", "Русский", BookGenre.FICTION, 544);
        book3.addAuthor(author3);

        Book book4 = new Book("Убийство в Восточном экспрессе", "Центрполиграф",
                LocalDate.of(1934, 1, 1), "978-5-227-05678-9",
                "Стеллаж 4, Полка А", "Русский", BookGenre.FICTION, 256);
        book4.addAuthor(author4);

        Book book5 = new Book("1984", "АСТ",
                LocalDate.of(1949, 6, 8), "978-5-17-080115-3",
                "Стеллаж 1, Полка Б", "Русский", BookGenre.FICTION, 320);
        book5.addAuthor(author5);

        Book book6 = new Book("451 градус по Фаренгейту", "Эксмо",
                LocalDate.of(1953, 10, 19), "978-5-04-093579-5",
                "Стеллаж 2, Полка Г", "Русский", BookGenre.FICTION, 208);
        book6.addAuthor(author6);

        Book book7 = new Book("Мастер и Маргарита", "Азбука",
                LocalDate.of(1967, 1, 1), "978-5-389-01678-9",
                "Стеллаж 3, Полка Д", "Русский", BookGenre.FICTION, 480);
        book7.addAuthor(author7);

        bookRepository.saveAll(java.util.List.of(book1, book2, book3, book4, book5, book6, book7));

        // ========== ЖУРНАЛЫ (5) ==========
        Magazine mag1 = new Magazine("National Geographic", "NatGeo Society",
                LocalDate.of(2023, 10, 1), "1234-5678",
                "Стенд журналов", "Английский", MagazineGenre.POPULAR_SCIENTIFIC, 120, true);

        Magazine mag2 = new Magazine("Вокруг света", "Вокруг света",
                LocalDate.of(2024, 1, 15), "0321-0669",
                "Стенд журналов", "Русский", MagazineGenre.POPULAR_SCIENTIFIC, 98, true);

        Magazine mag3 = new Magazine("Forbes", "Forbes Media",
                LocalDate.of(2024, 3, 10), "0015-6914",
                "Стенд журналов", "Английский", MagazineGenre.POPULAR_SCIENTIFIC, 86, true);

        Magazine mag4 = new Magazine("Наука и жизнь", "Наука и жизнь",
                LocalDate.of(2024, 2, 20), "0028-1263",
                "Стенд журналов", "Русский", MagazineGenre.SCIENTIFIC, 112, false);

        Magazine mag5 = new Magazine("Популярная механика", "Hearst Shkulev Media",
                LocalDate.of(2024, 4, 5), "0032-4558",
                "Стенд журналов", "Русский", MagazineGenre.POPULAR_SCIENTIFIC, 94, true);

        magazineRepository.saveAll(java.util.List.of(mag1, mag2, mag3, mag4, mag5));

        // ========== ЭКЗЕМПЛЯРЫ (17) ==========
        ItemCopy copy1Book1 = new ItemCopy(book1, "INV-BK-001");
        ItemCopy copy2Book1 = new ItemCopy(book1, "INV-BK-002");

        ItemCopy copy1Book2 = new ItemCopy(book2, "INV-BK-003");
        ItemCopy copy2Book2 = new ItemCopy(book2, "INV-BK-004");

        ItemCopy copy1Book3 = new ItemCopy(book3, "INV-BK-005");

        ItemCopy copy1Book4 = new ItemCopy(book4, "INV-BK-006");
        ItemCopy copy2Book4 = new ItemCopy(book4, "INV-BK-007");

        ItemCopy copy1Book5 = new ItemCopy(book5, "INV-BK-008");

        ItemCopy copy1Book6 = new ItemCopy(book6, "INV-BK-009");
        ItemCopy copy2Book6 = new ItemCopy(book6, "INV-BK-010");

        ItemCopy copy1Book7 = new ItemCopy(book7, "INV-BK-011");

        ItemCopy copy1Mag1 = new ItemCopy(mag1, "INV-MG-001");
        ItemCopy copy1Mag2 = new ItemCopy(mag2, "INV-MG-002");
        ItemCopy copy1Mag3 = new ItemCopy(mag3, "INV-MG-003");
        ItemCopy copy1Mag4 = new ItemCopy(mag4, "INV-MG-004");
        ItemCopy copy1Mag5 = new ItemCopy(mag5, "INV-MG-005");

        itemCopyRepository.saveAll(java.util.List.of(
                copy1Book1, copy2Book1,
                copy1Book2, copy2Book2,
                copy1Book3,
                copy1Book4, copy2Book4,
                copy1Book5,
                copy1Book6, copy2Book6,
                copy1Book7,
                copy1Mag1, copy1Mag2, copy1Mag3, copy1Mag4, copy1Mag5
        ));

        // ========== ПОЛЬЗОВАТЕЛИ (12) ==========

        // Админы (2)
        User admin1 = new User("Алексей", "Артемов", "Игоревич", "proectarmata@gmail.com",
                UserRole.ADMIN, passwordEncoder.encode("xxxx"), "LIB-0001");

        User admin2 = new User("Елена", "Смирнова", "Александровна", "elena@lims.com",
                UserRole.ADMIN, passwordEncoder.encode("admin123"), "LIB-0005");

        // Библиотекари (2)
        User librarian1 = new User("Петр", "Петров", "Сергеевич", "petr@test.com",
                UserRole.LIBRARIAN, passwordEncoder.encode("aBamaBlack17"), "LIB-0002");

        User librarian2 = new User("Мария", "Кузнецова", "Игоревна", "maria@test.com",
                UserRole.LIBRARIAN, passwordEncoder.encode("bibPass1"), "LIB-0006");

        // Читатели (8)
        User reader1 = User.createReader("Антонина", "Улицкая", "Ивановна",
                "antonina@test.com", "LIB-0003");

        User reader2 = User.createReader("Сергей", "Сергеев", "Петрович",
                "sergey@test.com", "LIB-0004");

        User reader3 = User.createReader("Алексей", "Волков", "Дмитриевич",
                "alexey@test.com", "LIB-0007");

        User reader4 = User.createReader("Ольга", "Морозова", "Сергеевна",
                "olga@test.com", "LIB-0008");

        User reader5 = User.createReader("Дмитрий", "Соколов", "Андреевич",
                "dmitry@test.com", "LIB-0009");

        User reader6 = User.createReader("Татьяна", "Белова", "Николаевна",
                "tatiana@test.com", "LIB-0010");

        User reader7 = User.createReader("Николай", "Попов", "Васильевич",
                "nikolay@test.com", "LIB-0011");

        User reader8 = User.createReader("Екатерина", "Васильева", "Алексеевна",
                "ekaterina@test.com", "LIB-0012");

        userRepository.saveAll(java.util.List.of(
                admin1, admin2,
                librarian1, librarian2,
                reader1, reader2, reader3, reader4, reader5, reader6, reader7, reader8
        ));

        // ========== ВЫДАЧИ (5) ==========
        Loan loan1 = new Loan(reader1, copy1Book1);
        loan1.setLoanDate(LocalDate.now().minusDays(10));
        loan1.setDueDate(LocalDate.now().plusDays(4));

        Loan loan2 = new Loan(reader2, copy1Book2);
        loan2.setLoanDate(LocalDate.now().minusDays(20));
        loan2.setDueDate(LocalDate.now().minusDays(6));

        Loan loan3 = new Loan(reader3, copy1Book3);
        loan3.setLoanDate(LocalDate.now().minusDays(5));
        loan3.setDueDate(LocalDate.now().plusDays(9));

        Loan loan4 = new Loan(reader4, copy1Mag1);
        loan4.setLoanDate(LocalDate.now().minusDays(15));
        loan4.setDueDate(LocalDate.now().minusDays(1));

        Loan loan5 = new Loan(reader5, copy1Book4);
        loan5.setLoanDate(LocalDate.now().minusDays(3));
        loan5.setDueDate(LocalDate.now().plusDays(11));

        loanRepository.saveAll(java.util.List.of(loan1, loan2, loan3, loan4, loan5));

        System.out.println("Тестовые данные успешно загружены!");
    }
}