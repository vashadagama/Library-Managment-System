package main.java.com.vasha.lims;

import main.java.com.vasha.lims.model.*;
import main.java.com.vasha.lims.enums.*;
import java.time.LocalDate;

public class Main {
    static void main(String[] args) {
        System.out.println("=== Запуск Системы Управления Библиотекой ===\n");

        Author author = new Author("Лев", "Толстой", LocalDate.of(1828, 9, 9));

        Book book = new Book(
                "Война и Мир", "Русский Вестник", LocalDate.of(1869, 1, 1),
                "978-5-17-083030-2", "Секция А, Полка 5", 2, "Русский",
                BookGenre.FICTION, 1225
        );
        book.addAuthor(author);

        User user = new User("Иван Иванов", "ivan@mail.com", UserRole.READER, "LC-001");

        System.out.println("До выдачи: " + book.getAvailableCopies() + " копий.");

        Loan loan1 = new Loan(user, book);
        Loan loan2 = new Loan(user, book);
        System.out.println(loan1);
        System.out.println(loan2);
        System.out.println("После выдачи: " + book.getAvailableCopies() + " копий.");

        loan1.renewLoan(7);
        System.out.println("После продления: " + loan1);

        loan1.returnItem();
        System.out.println("\nКнига возвращена!");
        System.out.println("Итого копий на полке: " + book.getAvailableCopies());
    }
}