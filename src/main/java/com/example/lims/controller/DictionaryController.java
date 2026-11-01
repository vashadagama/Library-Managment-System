package com.example.lims.controller;

import com.example.lims.dto.DictionaryItemDto;
import com.example.lims.service.DictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dictionaries")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/genres")
    public List<DictionaryItemDto> genres() {
        return dictionaryService.genres();
    }

    @GetMapping("/languages")
    public List<DictionaryItemDto> languages() {
        return dictionaryService.languages();
    }

    @GetMapping("/publishers")
    public List<DictionaryItemDto> publishers() {
        return dictionaryService.publishers();
    }

    @GetMapping("/locations")
    public List<DictionaryItemDto> locations() {
        return dictionaryService.locations();
    }

    @GetMapping("/roles")
    public List<DictionaryItemDto> roles() {
        return dictionaryService.roles();
    }

    @GetMapping("/user-statuses")
    public List<DictionaryItemDto> userStatuses() {
        return dictionaryService.userStatuses();
    }

    @GetMapping("/item-statuses")
    public List<DictionaryItemDto> itemStatuses() {
        return dictionaryService.itemStatuses();
    }

    @GetMapping("/loan-statuses")
    public List<DictionaryItemDto> loanStatuses() {
        return dictionaryService.loanStatuses();
    }

    @GetMapping("/notification-types")
    public List<DictionaryItemDto> notificationTypes() {
        return dictionaryService.notificationTypes();
    }

    @GetMapping("/audit-types")
    public List<DictionaryItemDto> auditTypes() {
        return dictionaryService.auditTypes();
    }
}
