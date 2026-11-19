package com.example.lims.controller;

import com.example.lims.service.AdminDictionaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Административный раздел: управление справочниками и системными параметрами.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminDictionaryService adminDictionaryService;

    public AdminController(AdminDictionaryService adminDictionaryService) {
        this.adminDictionaryService = adminDictionaryService;
    }

    @GetMapping("/dictionaries/{type}")
    public List<Map<String, Object>> listDictionary(@PathVariable String type) {
        return adminDictionaryService.list(type);
    }

    @PostMapping("/dictionaries/{type}")
    public Map<String, Object> createDictionaryEntry(@PathVariable String type,
                                                     @RequestBody Map<String, String> body) {
        return adminDictionaryService.create(type, body);
    }

    @PutMapping("/dictionaries/{type}/{id}")
    public Map<String, Object> updateDictionaryEntry(@PathVariable String type,
                                                     @PathVariable UUID id,
                                                     @RequestBody Map<String, String> body) {
        return adminDictionaryService.update(type, id, body);
    }

    @DeleteMapping("/dictionaries/{type}/{id}")
    public ResponseEntity<Void> deleteDictionaryEntry(@PathVariable String type,
                                                      @PathVariable UUID id) {
        adminDictionaryService.delete(type, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/system-parameters")
    public List<Map<String, Object>> listSystemParameters() {
        return adminDictionaryService.listParameters();
    }

    @PutMapping("/system-parameters/{key}")
    public Map<String, Object> updateSystemParameter(@PathVariable String key,
                                                     @RequestBody Map<String, String> body) {
        return adminDictionaryService.updateParameter(key, body.get("value"));
    }
}
