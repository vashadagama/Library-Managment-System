package com.example.lims.controller;

import com.example.lims.dto.BookDto;
import com.example.lims.enums.BookGenre;
import com.example.lims.model.Book;
import com.example.lims.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public BookDto create(@Valid @RequestBody Book book) {
        return bookService.createBook(book);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable UUID id, @Valid @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        bookService.deleteBook(id);
    }

    @GetMapping
    public Page<BookDto> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getAllBooks(page, size);
    }

    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/filter/genre")
    public List<BookDto> getByGenre(@RequestParam BookGenre genre) {
        return bookService.getBooksByGenre(genre);
    }

    @GetMapping("/filter/publisher")
    public List<BookDto> getByPublisher(@RequestParam String name) {
        return bookService.getBooksByPublisher(name);
    }

    @GetMapping("/search")
    public Page<BookDto> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BookGenre genre,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.searchBooks(title, genre, publisher, yearFrom, yearTo, page, size);
    }
}