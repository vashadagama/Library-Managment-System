package com.example.lims.service;

import com.example.lims.dto.MagazineDto;
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

    private MagazineDto toDto(Magazine magazine) {
        return new MagazineDto(
                magazine.getId(),
                magazine.getTitle(),
                magazine.getPublisher(),
                magazine.getPublicationDate(),
                magazine.getIssn(),
                magazine.getLocation(),
                magazine.getLanguage(),
                magazine.getGenre(),
                magazine.getPageCount(),
                magazine.isHasGlossyCover()
        );
    }

    private Page<MagazineDto> toDtoPage(Page<Magazine> page) {
        return page.map(this::toDto);
    }

    public MagazineDto getById(UUID id) {
        Magazine magazine = magazineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Журнал не найден"));
        return toDto(magazine);
    }

    public Page<MagazineDto> getAll(int page, int size) {
        return toDtoPage(magazineRepository.findAll(PageRequest.of(page, size)));
    }

    public Page<MagazineDto> search(String title, MagazineGenre genre, String publisher, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Magazine> resultPage = magazineRepository.searchMagazines(title, genre, publisher, pageable);
        return toDtoPage(resultPage);
    }

    @Transactional
    public MagazineDto create(Magazine magazine) {
        Magazine saved = magazineRepository.save(magazine);
        return toDto(saved);
    }

    @Transactional
    public MagazineDto update(UUID id, Magazine updatedMagazine) {
        Magazine existing = getByIdEntity(id);
        existing.setTitle(updatedMagazine.getTitle());
        existing.setPublisher(updatedMagazine.getPublisher());
        existing.setPublicationDate(updatedMagazine.getPublicationDate());
        existing.setLocation(updatedMagazine.getLocation());
        existing.setLanguage(updatedMagazine.getLanguage());
        existing.setIssn(updatedMagazine.getIssn());
        existing.setGenre(updatedMagazine.getGenre());
        existing.setPageCount(updatedMagazine.getPageCount());
        existing.setHasGlossyCover(updatedMagazine.isHasGlossyCover());
        Magazine saved = magazineRepository.save(existing);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Magazine magazine = getByIdEntity(id);
        magazineRepository.delete(magazine);
    }

    // Метод для получения сущности
    private Magazine getByIdEntity(UUID id) {
        return magazineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Журнал не найден"));
    }
}