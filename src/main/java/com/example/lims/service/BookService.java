package com.example.lims.service;

import com.example.lims.dto.BookDto;
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
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    private BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublicationDate(),
                book.getIsbn(),
                book.getLocation(),
                book.getLanguage(),
                book.getGenre(),
                book.getPageCount(),
                book.getAuthors()
        );
    }

    private Page<BookDto> toDtoPage(Page<Book> page) {
        return page.map(this::toDto);
    }

    @Transactional
    public BookDto createBook(Book book) {
        Book saved = bookRepository.save(book);
        return toDto(saved);
    }

    @Transactional
    public BookDto updateBook(UUID id, Book updatedBook) {
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
        Book saved = bookRepository.save(existing);
        return toDto(saved);
    }

    @Transactional
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена"));
        bookRepository.delete(book);
    }

    public Page<BookDto> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return toDtoPage(bookRepository.findAll(pageable));
    }

    public BookDto getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена"));
        return toDto(book);
    }

    public List<BookDto> getBooksByGenre(BookGenre genre) {
        return bookRepository.findByGenre(genre).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<BookDto> getBooksByPublisher(String publisher) {
        return bookRepository.findByPublisherIgnoreCase(publisher).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<BookDto> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<BookDto> searchByAuthor(String authorName) {
        return bookRepository.findByAuthorsLastNameIgnoreCase(authorName).stream().map(this::toDto).collect(Collectors.toList());
    }

    public Page<BookDto> searchBooks(String title, BookGenre genre, String publisher,
                                     Integer yearFrom, Integer yearTo, int page, int size) {
        LocalDate from = yearFrom != null ? LocalDate.of(yearFrom, 1, 1) : null;
        LocalDate to = yearTo != null ? LocalDate.of(yearTo, 12, 31) : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> resultPage = bookRepository.searchBooks(title, genre, publisher, from, to, pageable);
        return toDtoPage(resultPage);
    }
}