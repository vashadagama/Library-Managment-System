package com.example.lims.config;

import com.example.lims.entity.Location;
import com.example.lims.entity.Publisher;
import com.example.lims.entity.item.*;
import com.example.lims.entity.loan.Loan;
import com.example.lims.entity.status.*;
import com.example.lims.entity.user.User;
import com.example.lims.repository.*;
import com.example.lims.service.SystemParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final MagazineRepository magazineRepository;
    private final ItemCopyRepository itemCopyRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final PublisherRepository publisherRepository;
    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final ItemStatusRepository itemStatusRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final SystemParameterService systemParameterService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AuthorRepository authorRepository,
                           BookRepository bookRepository,
                           MagazineRepository magazineRepository,
                           ItemCopyRepository itemCopyRepository,
                           UserRepository userRepository,
                           LoanRepository loanRepository,
                           GenreRepository genreRepository,
                           LanguageRepository languageRepository,
                           PublisherRepository publisherRepository,
                           LocationRepository locationRepository,
                           RoleRepository roleRepository,
                           UserStatusRepository userStatusRepository,
                           ItemStatusRepository itemStatusRepository,
                           LoanStatusRepository loanStatusRepository,
                           SystemParameterService systemParameterService,
                           PasswordEncoder passwordEncoder) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.magazineRepository = magazineRepository;
        this.itemCopyRepository = itemCopyRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.publisherRepository = publisherRepository;
        this.locationRepository = locationRepository;
        this.roleRepository = roleRepository;
        this.userStatusRepository = userStatusRepository;
        this.itemStatusRepository = itemStatusRepository;
        this.loanStatusRepository = loanStatusRepository;
        this.systemParameterService = systemParameterService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("База данных уже содержит пользователей. Пропускаем инициализацию.");
            return;
        }

        logger.info("Начинаем заполнение базы тестовыми данными...");

        Genre fiction = genre("FICTION");
        Genre children = genre("CHILDREN");
        Genre scientific = genre("SCIENTIFIC");
        Genre popularScientific = genre("POPULAR_SCIENTIFIC");

        Language russian = language("Русский");
        Language english = language("Английский");

        Publisher rosmem = publisher("Росмэн", "Москва");
        Publisher ast = publisher("АСТ", "Москва");
        Publisher eksmo = publisher("Эксмо", "Москва");
        Publisher natGeo = publisher("NatGeo Society", "Вашингтон");
        Publisher aroundWorld = publisher("Вокруг света", "Москва");

        Location shelf1 = location("Стеллаж 1", "Полка А", null);
        Location shelf2 = location("Стеллаж 2", "Полка Б", null);
        Location shelf3 = location("Стеллаж 3", "Полка В", null);
        Location magazineStand = location("Стенд журналов", null, null);

        Author rowling = author("Джоан", "Роулинг", "Кэтлин");
        Author king = author("Стивен", "Кинг", "Эдвин");
        Author tolkien = author("Джон", "Толкин", "Рональд Руэл");
        Author orwell = author("Джордж", "Оруэлл", null);
        Author bulgakov = author("Михаил", "Булгаков", "Афанасьевич");

        Book book1 = book("Гарри Поттер и Философский камень", rosmem, russian, shelf1, children,
                LocalDate.of(1997, 6, 26), "978-5-353-00308-3", 399, rowling);
        Book book2 = book("Зеленая миля", ast, russian, shelf2, fiction,
                LocalDate.of(1996, 8, 29), "978-5-17-080115-2", 480, king);
        Book book3 = book("Властелин колец: Братство кольца", eksmo, russian, shelf3, fiction,
                LocalDate.of(1954, 7, 29), "978-5-04-093578-8", 544, tolkien);
        Book book4 = book("1984", ast, russian, shelf1, fiction,
                LocalDate.of(1949, 6, 8), "978-5-17-080115-3", 320, orwell);
        Book book5 = book("Мастер и Маргарита", eksmo, russian, shelf2, fiction,
                LocalDate.of(1967, 1, 1), "978-5-389-01678-9", 480, bulgakov);

        Magazine magazine1 = magazine("National Geographic", natGeo, english, magazineStand, popularScientific,
                LocalDate.of(2023, 10, 1), "1234-5678", 120, true);
        Magazine magazine2 = magazine("Вокруг света", aroundWorld, russian, magazineStand, popularScientific,
                LocalDate.of(2024, 1, 15), "0321-0669", 98, true);
        Magazine magazine3 = magazine("Наука и жизнь", aroundWorld, russian, magazineStand, scientific,
                LocalDate.of(2024, 2, 20), "0028-1263", 112, false);

        ItemStatus available = itemStatus("AVAILABLE");
        ItemStatus borrowed = itemStatus("BORROWED");

        ItemCopy copy1 = copy(book1, "INV-BK-001", available);
        ItemCopy copy2 = copy(book1, "INV-BK-002", available);
        ItemCopy copy3 = copy(book2, "INV-BK-003", available);
        ItemCopy copy4 = copy(book3, "INV-BK-004", available);
        ItemCopy copy5 = copy(book4, "INV-BK-005", available);
        ItemCopy copy6 = copy(book5, "INV-BK-006", available);
        ItemCopy copy7 = copy(magazine1, "INV-MG-001", available);
        ItemCopy copy8 = copy(magazine2, "INV-MG-002", available);
        ItemCopy copy9 = copy(magazine3, "INV-MG-003", available);

        User admin = user("Алексей", "Артемов", "Игоревич", "admin@lims.com",
                "ADMIN", "admin123", "LIB-0001");
        User librarian = user("Петр", "Петров", "Сергеевич", "librarian@lims.com",
                "LIBRARIAN", "librarian123", "LIB-0002");
        User reader1 = user("Антонина", "Улицкая", "Ивановна", "reader1@lims.com",
                "READER", null, "LIB-0003");
        User reader2 = user("Сергей", "Сергеев", "Петрович", "reader2@lims.com",
                "READER", null, "LIB-0004");
        User reader3 = user("Ольга", "Морозова", "Сергеевна", "reader3@lims.com",
                "READER", null, "LIB-0005");

        Loan loan1 = loan(reader1, copy1, 10, 4);
        Loan loan2 = loan(reader2, copy3, 20, -6);
        Loan loan3 = loan(reader3, copy7, 5, 9);

        copy1.setStatus(borrowed);
        copy3.setStatus(borrowed);
        copy7.setStatus(borrowed);
        itemCopyRepository.saveAll(List.of(copy1, copy3, copy7));

        loanRepository.saveAll(List.of(loan1, loan2, loan3));

        logger.info("Тестовые данные успешно загружены!");
    }

    private Author author(String firstName, String lastName, String middleName) {
        return authorRepository.save(new Author(firstName, lastName, middleName));
    }

    private Genre genre(String name) {
        return genreRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> genreRepository.save(newNamedGenre(name)));
    }

    private Genre newNamedGenre(String name) {
        Genre genre = new Genre();
        genre.setName(name);
        return genre;
    }

    private Language language(String name) {
        return languageRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Language language = new Language();
                    language.setName(name);
                    return languageRepository.save(language);
                });
    }

    private Publisher publisher(String name, String city) {
        return publisherRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Publisher publisher = new Publisher();
                    publisher.setName(name);
                    publisher.setCity(city);
                    return publisherRepository.save(publisher);
                });
    }

    private Location location(String hall, String shelf, String rack) {
        Location location = new Location();
        location.setHall(hall);
        location.setShelf(shelf);
        location.setRack(rack);
        return locationRepository.save(location);
    }

    private Book book(String title, Publisher publisher, Language language, Location location,
                       Genre genre, LocalDate publicationDate, String isbn, int pageCount, Author... authors) {
        Book book = new Book();
        book.setTitle(title);
        book.setPublisher(publisher);
        book.setLanguage(language);
        book.setLocation(location);
        book.setGenre(genre);
        book.setPublicationDate(publicationDate);
        book.setIsbn(isbn);
        book.setPageCount(pageCount);
        for (Author author : authors) {
            book.addAuthor(author);
        }
        return bookRepository.save(book);
    }

    private Magazine magazine(String title, Publisher publisher, Language language, Location location,
                              Genre genre, LocalDate publicationDate, String issn,
                              int pageCount, boolean hasGlossyCover) {
        Magazine magazine = new Magazine();
        magazine.setTitle(title);
        magazine.setPublisher(publisher);
        magazine.setLanguage(language);
        magazine.setLocation(location);
        magazine.setGenre(genre);
        magazine.setPublicationDate(publicationDate);
        magazine.setIssn(issn);
        magazine.setPageCount(pageCount);
        magazine.setHasGlossyCover(hasGlossyCover);
        return magazineRepository.save(magazine);
    }

    private ItemCopy copy(LibraryItem item, String inventoryNumber, ItemStatus status) {
        ItemCopy copy = new ItemCopy();
        copy.setItem(item);
        copy.setInventoryNumber(inventoryNumber);
        copy.setAcquisitionDate(LocalDate.now().minusMonths(6));
        copy.setStatus(status);
        return itemCopyRepository.save(copy);
    }

    private User user(String firstName, String lastName, String middleName, String email,
                      String roleName, String password, String libraryCardNumber) {
        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new IllegalStateException("Роль не найдена: " + roleName));
        UserStatus status = userStatusRepository.findByNameIgnoreCase("ACTIVE")
                .orElseThrow(() -> new IllegalStateException("Статус ACTIVE не найден"));

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMiddleName(middleName);
        user.setEmail(email);
        user.setLibraryCardNumber(libraryCardNumber);
        user.setRole(role);
        user.setStatus(status);
        user.setMaxActiveLoans(systemParameterService.getInt("max_active_loans_default", 5));
        user.setPasswordHash(password != null ? passwordEncoder.encode(password) : null);
        return userRepository.save(user);
    }

    private Loan loan(User user, ItemCopy copy, int daysAgo, int dueInDays) {
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setCopy(copy);
        loan.setLoanDate(LocalDate.now().minusDays(daysAgo));
        loan.setDueDate(LocalDate.now().plusDays(dueInDays));
        loan.setRenewalCount(0);
        loan.setStatus(loanStatus("ACTIVE"));
        return loanRepository.save(loan);
    }

    private ItemStatus itemStatus(String name) {
        return itemStatusRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalStateException("Статус экземпляра не найден: " + name));
    }

    private LoanStatus loanStatus(String name) {
        return loanStatusRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalStateException("Статус выдачи не найден: " + name));
    }
}
