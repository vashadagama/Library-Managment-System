package com.example.lims.service;

import com.example.lims.dto.BookCreateDto;
import com.example.lims.dto.BookDto;
import com.example.lims.enums.BookGenre;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Author;
import com.example.lims.model.Book;
import com.example.lims.repository.AuthorRepository;
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

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private BookCreateDto createTestDto() {
        BookCreateDto dto = new BookCreateDto();
        dto.setTitle("Test Book");
        dto.setPublisher("Test Publisher");
        dto.setPublicationDate(LocalDate.now());
        dto.setIsbn("123-4567890");
        dto.setLocation("A1");
        dto.setLanguage("Russian");
        dto.setGenre(BookGenre.FICTION);
        dto.setPageCount(300);
        dto.setAuthorIds(List.of(UUID.randomUUID()));
        return dto;
    }

    private Book createTestBook(UUID id) {
        Book book = new Book();
        book.setTitle("Test Book");
        book.setPublisher("Test Publisher");
        book.setPublicationDate(LocalDate.now());
        book.setIsbn("123-4567890");
        book.setLocation("A1");
        book.setLanguage("Russian");
        book.setGenre(BookGenre.FICTION);
        book.setPageCount(300);
        return book;
    }

    @Test
    void createBook() {
        UUID id = UUID.randomUUID();
        BookCreateDto dto = createTestDto();
        Book book = createTestBook(id);

        when(authorRepository.findAllById(dto.getAuthorIds())).thenReturn(List.of(new Author()));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto result = bookService.createBook(dto);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(authorRepository, times(1)).findAllById(dto.getAuthorIds());
    }

    @Test
    void getBookById() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        BookDto result = bookService.getBookById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void getBookByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(id));
    }

    @Test
    void getAllBooks() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Book book1 = createTestBook(id1);
        Book book2 = createTestBook(id2);
        Page<Book> page = new PageImpl<>(List.of(book1, book2));

        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<BookDto> result = bookService.getAllBooks(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(id1, result.getContent().get(0).getId());
        assertEquals(id2, result.getContent().get(1).getId());
    }

    @Test
    void searchByTitle() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        when(bookRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(book));

        List<BookDto> result = bookService.searchByTitle("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
    }

    @Test
    void searchByAuthor() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        when(bookRepository.findByAuthorsLastNameIgnoreCase("Author")).thenReturn(List.of(book));

        List<BookDto> result = bookService.searchByAuthor("Author");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
    }

    @Test
    void updateBook() {
        UUID id = UUID.randomUUID();
        Book existing = createTestBook(id);
        BookCreateDto dto = createTestDto();
        dto.setTitle("Updated Title");

        when(bookRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorRepository.findAllById(dto.getAuthorIds())).thenReturn(List.of(new Author()));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        BookDto result = bookService.updateBook(id, dto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(bookRepository, times(1)).save(existing);
        verify(authorRepository, times(1)).findAllById(dto.getAuthorIds());
    }

    @Test
    void deleteBook() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(book);

        bookService.deleteBook(id);

        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void deleteBookNotFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(id));
    }

    @Test
    void getBooksByGenre() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        when(bookRepository.findByGenre(BookGenre.FICTION)).thenReturn(List.of(book));

        List<BookDto> result = bookService.getBooksByGenre(BookGenre.FICTION);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
    }

    @Test
    void getBooksByPublisher() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        when(bookRepository.findByPublisherIgnoreCase("Test Publisher")).thenReturn(List.of(book));

        List<BookDto> result = bookService.getBooksByPublisher("Test Publisher");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
    }

    @Test
    void searchBooks() {
        UUID id = UUID.randomUUID();
        Book book = createTestBook(id);
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.searchBooks(any(), any(), any(), any(), any(), any())).thenReturn(page);

        Page<BookDto> result = bookService.searchBooks("Test", BookGenre.FICTION, "Publisher", 2020, 2023, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(id, result.getContent().get(0).getId());
    }
}