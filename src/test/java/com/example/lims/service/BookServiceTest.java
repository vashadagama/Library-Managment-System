package com.example.lims.service;

import com.example.lims.dto.BookCreateDto;
import com.example.lims.dto.BookDto;
import com.example.lims.entity.Location;
import com.example.lims.entity.Publisher;
import com.example.lims.entity.item.Author;
import com.example.lims.entity.item.Book;
import com.example.lims.entity.status.Genre;
import com.example.lims.entity.status.Language;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.AuthorRepository;
import com.example.lims.repository.BookRepository;
import com.example.lims.repository.GenreRepository;
import com.example.lims.repository.LanguageRepository;
import com.example.lims.repository.LocationRepository;
import com.example.lims.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private AuditService auditService;

    @InjectMocks private BookService bookService;

    private static Genre genre(UUID id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("FICTION");
        return genre;
    }

    private static Language language(UUID id) {
        Language language = new Language();
        language.setId(id);
        language.setName("Русский");
        return language;
    }

    @Test
    void createBookResolvesClassifiersAndAuthors() {
        UUID genreId = UUID.randomUUID();
        UUID languageId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Publisher publisher = new Publisher();
        publisher.setId(publisherId);
        publisher.setName("Издательство");
        Author author = new Author("Иван", "Иванов", null);
        author.setId(authorId);

        BookCreateDto dto = new BookCreateDto();
        dto.setTitle("Книга");
        dto.setPublicationDate(LocalDate.of(2020, 1, 1));
        dto.setGenreId(genreId);
        dto.setLanguageId(languageId);
        dto.setPublisherId(publisherId);
        dto.setAuthorIds(List.of(authorId));

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre(genreId)));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language(languageId)));
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(authorId))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(UUID.randomUUID());
            return book;
        });

        BookDto result = bookService.createBook(dto);

        assertEquals("Книга", result.getTitle());
        assertEquals(genreId, result.getGenreId());
        assertEquals("FICTION", result.getGenreName());
        assertEquals(languageId, result.getLanguageId());
        assertEquals("Русский", result.getLanguageName());
        assertEquals(publisherId, result.getPublisherId());
        assertEquals(authorId, result.getAuthors().getFirst().getId());
    }

    @Test
    void createBookRejectsMissingAuthor() {
        UUID genreId = UUID.randomUUID();
        UUID languageId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookCreateDto dto = new BookCreateDto();
        dto.setTitle("Книга");
        dto.setPublicationDate(LocalDate.of(2020, 1, 1));
        dto.setGenreId(genreId);
        dto.setLanguageId(languageId);
        dto.setAuthorIds(List.of(authorId));

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre(genreId)));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language(languageId)));
        when(authorRepository.findAllById(List.of(authorId))).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(dto));
    }

    @Test
    void getBookByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(id));
    }
}
