package com.example.lims.service;

import com.example.lims.dto.MagazineCreateDto;
import com.example.lims.dto.MagazineDto;
import com.example.lims.entity.item.Magazine;
import com.example.lims.entity.status.Genre;
import com.example.lims.entity.status.Language;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.GenreRepository;
import com.example.lims.repository.LanguageRepository;
import com.example.lims.repository.LocationRepository;
import com.example.lims.repository.MagazineRepository;
import com.example.lims.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MagazineServiceTest {

    @Mock private MagazineRepository magazineRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private AuditService auditService;

    @InjectMocks private MagazineService magazineService;

    private static Genre genre(UUID id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("POPULAR_SCIENTIFIC");
        return genre;
    }

    private static Language language(UUID id) {
        Language language = new Language();
        language.setId(id);
        language.setName("Русский");
        return language;
    }

    @Test
    void createMagazine() {
        UUID genreId = UUID.randomUUID();
        UUID languageId = UUID.randomUUID();
        MagazineCreateDto dto = new MagazineCreateDto();
        dto.setTitle("Журнал");
        dto.setPublicationDate(LocalDate.of(2024, 1, 1));
        dto.setGenreId(genreId);
        dto.setLanguageId(languageId);
        dto.setHasGlossyCover(true);

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre(genreId)));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language(languageId)));
        when(magazineRepository.save(any(Magazine.class))).thenAnswer(invocation -> {
            Magazine magazine = invocation.getArgument(0);
            magazine.setId(UUID.randomUUID());
            return magazine;
        });

        MagazineDto result = magazineService.create(dto);

        assertEquals("Журнал", result.getTitle());
        assertEquals(genreId, result.getGenreId());
        assertEquals("POPULAR_SCIENTIFIC", result.getGenreName());
        assertEquals(true, result.isHasGlossyCover());
    }

    @Test
    void createMagazineRequiresGenre() {
        MagazineCreateDto dto = new MagazineCreateDto();
        dto.setTitle("Журнал");
        dto.setPublicationDate(LocalDate.of(2024, 1, 1));
        dto.setLanguageId(UUID.randomUUID());

        assertThrows(ResourceNotFoundException.class, () -> magazineService.create(dto));
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(magazineRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> magazineService.getById(id));
    }
}
