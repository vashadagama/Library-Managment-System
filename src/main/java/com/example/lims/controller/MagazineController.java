package com.example.lims.controller;

import com.example.lims.model.Magazine;
import com.example.lims.service.MagazineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/magazines")
public class MagazineController {
    private final MagazineService magazineService;

    public MagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @GetMapping
    public List<Magazine> getAll() {
        return magazineService.getAll();
    }

    @PostMapping
    public Magazine create(@Valid @RequestBody Magazine magazine) {
        return magazineService.save(magazine);
    }
}