package com.example.lims.service;

import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Author;
import com.example.lims.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Test
    void getAllAuthors() {
        List<Author> authors = List.of(new Author());
        when(authorRepository.findAll()).thenReturn(authors);

        List<Author> result = authorService.getAllAuthors();

        assertEquals(1, result.size());
        verify(authorRepository).findAll();
    }

    @Test
    void createAuthor() {
        Author author = new Author("Иван", "Иванов", null);
        when(authorRepository.save(author)).thenReturn(author);

        Author result = authorService.createAuthor(author);

        assertNotNull(result);
        verify(authorRepository).save(author);
    }

    @Test
    void getAuthorById() {
        UUID id = UUID.randomUUID();
        Author author = new Author();
        when(authorRepository.findById(id)).thenReturn(Optional.of(author));

        Author result = authorService.getAuthorById(id);

        assertNotNull(result);
        verify(authorRepository).findById(id);
    }

    @Test
    void getAuthorByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorService.getAuthorById(id));
    }
}