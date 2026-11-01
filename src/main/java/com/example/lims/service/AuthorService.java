package com.example.lims.service;

import com.example.lims.dto.AuthorDto;
import com.example.lims.entity.item.Author;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.AuthorRepository;
import com.example.lims.repository.LibraryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final LibraryItemRepository libraryItemRepository;

    public AuthorService(AuthorRepository authorRepository, LibraryItemRepository libraryItemRepository) {
        this.authorRepository = authorRepository;
        this.libraryItemRepository = libraryItemRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public AuthorDto createAuthor(AuthorDto dto) {
        Author author = new Author(dto.getFirstName(), dto.getLastName(), dto.getMiddleName());
        return toDto(authorRepository.save(author));
    }

    @Transactional(readOnly = true)
    public AuthorDto getAuthorById(UUID id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public AuthorDto updateAuthor(UUID id, AuthorDto dto) {
        Author existing = getEntity(id);
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setMiddleName(dto.getMiddleName());
        return toDto(authorRepository.save(existing));
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        Author author = getEntity(id);
        if (libraryItemRepository.existsByAuthors_Id(id)) {
            throw new BusinessLogicException("Нельзя удалить автора, у которого есть произведения. Сначала удалите или отвяжите произведения.");
        }
        authorRepository.delete(author);
    }

    private Author getEntity(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Автор не найден"));
    }

    private AuthorDto toDto(Author author) {
        return new AuthorDto(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getMiddleName(),
                author.getFullName()
        );
    }
}
