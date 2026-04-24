package com.example.lims.controller;

import com.example.lims.enums.MagazineGenre;
import com.example.lims.model.Magazine;
import com.example.lims.service.MagazineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/magazines")
public class MagazineController {
    private final MagazineService magazineService;

    public MagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @GetMapping
    public Page<Magazine> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) MagazineGenre genre,
            @RequestParam(required = false) String publisher
    ) {
        if (genre != null || publisher != null) {
            return magazineService.search(genre, publisher, page, size);
        }
        return magazineService.getAll(page, size);
    }

    @PostMapping
    public Magazine create(@Valid @RequestBody Magazine magazine) {
        return magazineService.save(magazine);
    }
}