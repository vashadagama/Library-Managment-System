package com.example.lims.controller;

import com.example.lims.enums.MagazineGenre;
import com.example.lims.model.Magazine;
import com.example.lims.service.MagazineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/magazines")
public class MagazineController {
    private final MagazineService magazineService;

    public MagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @GetMapping("/{id}")
    public Magazine getById(@PathVariable UUID id) {
        return magazineService.getById(id);
    }

    @GetMapping
    public Page<Magazine> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) MagazineGenre genre,
            @RequestParam(required = false) String publisher
    ) {
        if (title != null || genre != null || publisher != null) {
            return magazineService.search(title, genre, publisher, page, size);
        }
        return magazineService.getAll(page, size);
    }

    @PostMapping
    public Magazine create(@Valid @RequestBody Magazine magazine) {
        return magazineService.create(magazine);
    }

    @PutMapping("/{id}")
    public Magazine update(@PathVariable UUID id, @Valid @RequestBody Magazine magazine) {
        return magazineService.update(id, magazine);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        magazineService.delete(id);
    }
}