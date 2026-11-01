package com.example.lims.service;

import com.example.lims.dto.MagazineCreateDto;
import com.example.lims.dto.MagazineDto;
import com.example.lims.entity.Location;
import com.example.lims.entity.Publisher;
import com.example.lims.entity.item.Magazine;
import com.example.lims.entity.status.Genre;
import com.example.lims.entity.status.Language;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.*;
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
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final LanguageRepository languageRepository;
    private final LocationRepository locationRepository;

    public MagazineService(MagazineRepository magazineRepository,
                           GenreRepository genreRepository,
                           PublisherRepository publisherRepository,
                           LanguageRepository languageRepository,
                           LocationRepository locationRepository) {
        this.magazineRepository = magazineRepository;
        this.genreRepository = genreRepository;
        this.publisherRepository = publisherRepository;
        this.languageRepository = languageRepository;
        this.locationRepository = locationRepository;
    }

    public MagazineDto getById(UUID id) {
        return toDto(getEntity(id));
    }

    public Page<MagazineDto> getAll(int page, int size) {
        return magazineRepository.findAll(PageRequest.of(page, size)).map(this::toDto);
    }

    public Page<MagazineDto> search(String title, UUID genreId, UUID publisherId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return magazineRepository.searchMagazines(title, genreId, publisherId, pageable).map(this::toDto);
    }

    @Transactional
    public MagazineDto create(MagazineCreateDto dto) {
        Magazine magazine = new Magazine();
        applyDto(magazine, dto);
        return toDto(magazineRepository.save(magazine));
    }

    @Transactional
    public MagazineDto update(UUID id, MagazineCreateDto dto) {
        Magazine existing = getEntity(id);
        applyDto(existing, dto);
        return toDto(magazineRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        magazineRepository.delete(getEntity(id));
    }

    private void applyDto(Magazine magazine, MagazineCreateDto dto) {
        magazine.setTitle(dto.getTitle());
        magazine.setPublicationDate(dto.getPublicationDate());
        magazine.setIssn(dto.getIssn());
        magazine.setPageCount(dto.getPageCount());
        magazine.setHasGlossyCover(dto.isHasGlossyCover());
        magazine.setGenre(resolveGenre(dto.getGenreId()));
        magazine.setLanguage(resolveLanguage(dto.getLanguageId()));
        magazine.setPublisher(resolvePublisher(dto.getPublisherId()));
        magazine.setLocation(resolveLocation(dto.getLocationId()));
    }

    private Genre resolveGenre(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("Жанр не указан");
        }
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден"));
    }

    private Language resolveLanguage(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("Язык не указан");
        }
        return languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Язык не найден"));
    }

    private Publisher resolvePublisher(UUID id) {
        return id == null ? null : publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Издатель не найден"));
    }

    private Location resolveLocation(UUID id) {
        return id == null ? null : locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Расположение не найдено"));
    }

    private Magazine getEntity(UUID id) {
        return magazineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Журнал не найден"));
    }

    private MagazineDto toDto(Magazine magazine) {
        return new MagazineDto(
                magazine.getId(),
                magazine.getTitle(),
                magazine.getPublisher() != null ? magazine.getPublisher().getId() : null,
                magazine.getPublisher() != null ? magazine.getPublisher().getName() : null,
                magazine.getPublicationDate(),
                magazine.getIssn(),
                magazine.getLocation() != null ? magazine.getLocation().getId() : null,
                locationName(magazine.getLocation()),
                magazine.getLanguage() != null ? magazine.getLanguage().getId() : null,
                magazine.getLanguage() != null ? magazine.getLanguage().getName() : null,
                magazine.getGenre() != null ? magazine.getGenre().getId() : null,
                magazine.getGenre() != null ? magazine.getGenre().getName() : null,
                magazine.getPageCount(),
                Boolean.TRUE.equals(magazine.getHasGlossyCover())
        );
    }

    private String locationName(Location location) {
        if (location == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (location.getHall() != null && !location.getHall().isBlank()) sb.append(location.getHall());
        if (location.getShelf() != null && !location.getShelf().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(location.getShelf());
        }
        if (location.getRack() != null && !location.getRack().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(location.getRack());
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
