package com.example.lims.service;

import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Author;
import com.example.lims.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @Transactional
    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Transactional(readOnly = true)
    public Author getAuthorById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Автор не найден"));
    }

    @Transactional
    public Author updateAuthor(UUID id, Author authorDetails) {
        Author existing = getAuthorById(id);
        existing.setFirstName(authorDetails.getFirstName());
        existing.setLastName(authorDetails.getLastName());
        existing.setMiddleName(authorDetails.getMiddleName());
        return authorRepository.save(existing);
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        Author author = getAuthorById(id);
        if (author.getWorks() != null && !author.getWorks().isEmpty()) {
            throw new BusinessLogicException("Нельзя удалить автора, у которого есть произведения. Сначала удалите или отвяжите произведения.");
        }
        authorRepository.delete(author);
    }
}