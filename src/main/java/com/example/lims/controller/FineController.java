package com.example.lims.controller;

import com.example.lims.dto.FineDto;
import com.example.lims.service.FineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public List<FineDto> getFines(@RequestParam(required = false) Boolean paid) {
        return fineService.getFines(paid);
    }

    @PatchMapping("/{id}/pay")
    public FineDto payFine(@PathVariable UUID id) {
        return fineService.payFine(id);
    }
}
