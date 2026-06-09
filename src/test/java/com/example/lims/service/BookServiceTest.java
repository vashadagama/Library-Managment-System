package com.example.lims.service;

import com.example.lims.enums.BookGenre;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Book;
import com.example.lims.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void createBook() {
        Book book = new Book();
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.createBook(book);
        assertNotNull(result);
        verify(bookRepository).save(book);
    }

    @Test
    void getBookById() {
        UUID id = UUID.randomUUID();
        Book book = new Book();
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(id);
        assertNotNull(result);
    }

    @Test
    void getBookByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(id));
    }

    @Test
    void getAllBooks() {
        Page<Book> page = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<Book> result = bookService.getAllBooks(0, 10);
        assertNotNull(result);
    }
}