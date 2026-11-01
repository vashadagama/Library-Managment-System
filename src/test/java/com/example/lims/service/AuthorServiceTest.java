package com.example.lims.service;

import com.example.lims.dto.AuthorDto;
import com.example.lims.entity.item.Author;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.AuthorRepository;
import com.example.lims.repository.LibraryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock private AuthorRepository authorRepository;
    @Mock private LibraryItemRepository libraryItemRepository;

    @InjectMocks private AuthorService authorService;

    @Test
    void createAuthor() {
        AuthorDto dto = new AuthorDto(null, "Иван", "Иванов", null, null);

        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(UUID.randomUUID());
            return author;
        });

        AuthorDto result = authorService.createAuthor(dto);

        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        assertEquals("Иван Иванов", result.getFullName());
    }

    @Test
    void deleteAuthorWithWorksIsForbidden() {
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.of(new Author()));
        when(libraryItemRepository.existsByAuthors_Id(id)).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> authorService.deleteAuthor(id));
    }

    @Test
    void getAuthorByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorService.getAuthorById(id));
    }
}
