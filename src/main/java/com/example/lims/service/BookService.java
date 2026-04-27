package com.example.lims.service;

import com.example.lims.enums.BookGenre;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Book;
import com.example.lims.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(UUID id, Book updatedBook) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена"));
        existing.setTitle(updatedBook.getTitle());
        existing.setPublisher(updatedBook.getPublisher());
        existing.setPublicationDate(updatedBook.getPublicationDate());
        existing.setLocation(updatedBook.getLocation());
        existing.setLanguage(updatedBook.getLanguage());
        existing.setIsbn(updatedBook.getIsbn());
        existing.setGenre(updatedBook.getGenre());
        existing.setPageCount(updatedBook.getPageCount());
        // авторы обновляются отдельно через связь many-to-many, если потребуется
        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена"));
        bookRepository.delete(book);
    }

    public Page<Book> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable);
    }

    public Book getBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена"));
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> searchByAuthor(String authorName) {
        return bookRepository.findByAuthorsLastNameIgnoreCase(authorName);
    }

    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String title, BookGenre genre, String publisher,
                                  Integer yearFrom, Integer yearTo, int page, int size) {
        LocalDate from = yearFrom != null ? LocalDate.of(yearFrom, 1, 1) : null;
        LocalDate to = yearTo != null ? LocalDate.of(yearTo, 12, 31) : null;
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.searchBooks(title, genre, publisher, from, to, pageable);
    }
}