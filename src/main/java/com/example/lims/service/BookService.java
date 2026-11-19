package com.example.lims.service;

import com.example.lims.dto.AuthorDto;
import com.example.lims.dto.BookCreateDto;
import com.example.lims.dto.BookDto;
import com.example.lims.entity.Location;
import com.example.lims.entity.Publisher;
import com.example.lims.entity.item.Author;
import com.example.lims.entity.item.Book;
import com.example.lims.entity.status.Genre;
import com.example.lims.entity.status.Language;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.*;
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
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final LanguageRepository languageRepository;
    private final LocationRepository locationRepository;
    private final AuditService auditService;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       GenreRepository genreRepository,
                       PublisherRepository publisherRepository,
                       LanguageRepository languageRepository,
                       LocationRepository locationRepository,
                       AuditService auditService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.publisherRepository = publisherRepository;
        this.languageRepository = languageRepository;
        this.locationRepository = locationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public BookDto createBook(BookCreateDto dto) {
        Book book = new Book();
        applyDto(book, dto);
        Book saved = bookRepository.save(book);
        auditService.log("CREATE", "BOOK", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public BookDto updateBook(UUID id, BookCreateDto dto) {
        Book existing = getEntity(id);
        applyDto(existing, dto);
        Book saved = bookRepository.save(existing);
        auditService.log("UPDATE", "BOOK", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public void deleteBook(UUID id) {
        Book book = getEntity(id);
        auditService.log("DELETE", "BOOK", book.getId());
        bookRepository.delete(book);
    }

    public Page<BookDto> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable).map(this::toDto);
    }

    public BookDto getBookById(UUID id) {
        return toDto(getEntity(id));
    }

    public List<BookDto> getBooksByGenre(UUID genreId) {
        return bookRepository.findByGenre_Id(genreId).stream().map(this::toDto).toList();
    }

    public List<BookDto> getBooksByPublisher(String publisher) {
        return bookRepository.findByPublisher_NameIgnoreCase(publisher).stream().map(this::toDto).toList();
    }

    public List<BookDto> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream().map(this::toDto).toList();
    }

    public List<BookDto> searchByAuthor(String authorName) {
        return bookRepository.findByAuthorLastName(authorName).stream().map(this::toDto).toList();
    }

    public Page<BookDto> searchBooks(String title, UUID genreId, UUID publisherId,
                                     Integer yearFrom, Integer yearTo, int page, int size) {
        LocalDate from = yearFrom != null ? LocalDate.of(yearFrom, 1, 1) : null;
        LocalDate to = yearTo != null ? LocalDate.of(yearTo, 12, 31) : null;
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.searchBooks(title, genreId, publisherId, from, to, pageable).map(this::toDto);
    }

    private void applyDto(Book book, BookCreateDto dto) {
        book.setTitle(dto.getTitle());
        book.setPublicationDate(dto.getPublicationDate());
        book.setIsbn(dto.getIsbn());
        book.setPageCount(dto.getPageCount());
        book.setGenre(resolveGenre(dto.getGenreId()));
        book.setLanguage(resolveLanguage(dto.getLanguageId()));
        book.setPublisher(resolvePublisher(dto.getPublisherId()));
        book.setLocation(resolveLocation(dto.getLocationId()));

        book.getAuthors().clear();
        if (dto.getAuthorIds() != null && !dto.getAuthorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(dto.getAuthorIds());
            if (authors.size() != dto.getAuthorIds().size()) {
                throw new ResourceNotFoundException("Один или несколько авторов не найдены");
            }
            authors.forEach(book::addAuthor);
        }
    }

    private Genre resolveGenre(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("Жанр не указан");
        }
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден"));
    }

    private Language resolveLanguage(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("Язык не указан");
        }
        return languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Язык не найден"));
    }

    private Publisher resolvePublisher(UUID id) {
        return id == null ? null : publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Издатель не найден"));
    }

    private Location resolveLocation(UUID id) {
        return id == null ? null : locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Расположение не найдено"));
    }

    private Book getEntity(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена"));
    }

    private BookDto toDto(Book book) {
        List<AuthorDto> authors = book.getAuthors().stream()
                .map(a -> new AuthorDto(a.getId(), a.getFirstName(), a.getLastName(),
                        a.getMiddleName(), a.getFullName()))
                .toList();
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getPublisher() != null ? book.getPublisher().getId() : null,
                book.getPublisher() != null ? book.getPublisher().getName() : null,
                book.getPublicationDate(),
                book.getIsbn(),
                book.getLocation() != null ? book.getLocation().getId() : null,
                locationName(book.getLocation()),
                book.getLanguage() != null ? book.getLanguage().getId() : null,
                book.getLanguage() != null ? book.getLanguage().getName() : null,
                book.getGenre() != null ? book.getGenre().getId() : null,
                book.getGenre() != null ? book.getGenre().getName() : null,
                book.getPageCount(),
                authors
        );
    }

    private String locationName(Location location) {
        if (location == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (location.getHall() != null && !location.getHall().isBlank()) sb.append(location.getHall());
        if (location.getShelf() != null && !location.getShelf().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(location.getShelf());
        }
        if (location.getRack() != null && !location.getRack().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(location.getRack());
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
