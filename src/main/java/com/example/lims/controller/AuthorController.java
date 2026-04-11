package com.example.lims.controller;

import com.example.lims.model.Author;
import com.example.lims.service.AuthorService;
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
    public List<Author> getAll() {
        return authorService.getAllAuthors();
    }

    @PostMapping
    public Author create(@RequestBody Author author) {
        return authorService.createAuthor(author);
    }

    @GetMapping("/{id}")
    public Author getById(@PathVariable UUID id) {
        return authorService.getAuthorById(id);
    }
}