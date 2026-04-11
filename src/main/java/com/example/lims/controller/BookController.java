package com.example.lims.controller;

import com.example.lims.model.Book;
import com.example.lims.repository.BookRepository;
import com.example.lims.service.BookService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;

    public BookController(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    @PostMapping
    public Book create(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    @GetMapping
    public Page<Book> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getAllBooks(page, size);
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/filter/genre")
    public List<Book> getByGenre(@RequestParam com.example.lims.enums.BookGenre genre) {
        return bookRepository.findByGenre(genre);
    }

    @GetMapping("/filter/publisher")
    public List<Book> getByPublisher(@RequestParam String name) {
        return bookRepository.findByPublisherIgnoreCase(name);
    }
}