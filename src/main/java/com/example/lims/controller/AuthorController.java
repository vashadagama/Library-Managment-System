package com.example.lims.controller;

import com.example.lims.dto.AuthorDto;
import com.example.lims.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorDto> getAll() {
        return authorService.getAllAuthors();
    }

    @PostMapping
    public AuthorDto create(@Valid @RequestBody AuthorDto author) {
        return authorService.createAuthor(author);
    }

    @GetMapping("/{id}")
    public AuthorDto getById(@PathVariable UUID id) {
        return authorService.getAuthorById(id);
    }

    @PutMapping("/{id}")
    public AuthorDto update(@PathVariable UUID id, @Valid @RequestBody AuthorDto author) {
        return authorService.updateAuthor(id, author);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        authorService.deleteAuthor(id);
    }
}
