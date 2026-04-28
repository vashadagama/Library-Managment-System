package com.example.lims.service;

import com.example.lims.enums.MagazineGenre;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Magazine;
import com.example.lims.repository.MagazineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MagazineService {
    private final MagazineRepository magazineRepository;

    public MagazineService(MagazineRepository magazineRepository) {
        this.magazineRepository = magazineRepository;
    }

    public Magazine getById(UUID id) {
        return magazineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Журнал не найден"));
    }

    public Page<Magazine> getAll(int page, int size) {
        return magazineRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Magazine> search(String title, MagazineGenre genre, String publisher, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return magazineRepository.searchMagazines(title, genre, publisher, pageable);
    }

    @Transactional
    public Magazine create(Magazine magazine) {
        return magazineRepository.save(magazine);
    }

    @Transactional
    public Magazine update(UUID id, Magazine updatedMagazine) {
        Magazine existing = getById(id);
        existing.setTitle(updatedMagazine.getTitle());
        existing.setPublisher(updatedMagazine.getPublisher());
        existing.setPublicationDate(updatedMagazine.getPublicationDate());
        existing.setLocation(updatedMagazine.getLocation());
        existing.setLanguage(updatedMagazine.getLanguage());
        existing.setIssn(updatedMagazine.getIssn());
        existing.setGenre(updatedMagazine.getGenre());
        existing.setPageCount(updatedMagazine.getPageCount());
        existing.setHasGlossyCover(updatedMagazine.isHasGlossyCover());
        return magazineRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        Magazine magazine = getById(id);
        magazineRepository.delete(magazine);
    }
}