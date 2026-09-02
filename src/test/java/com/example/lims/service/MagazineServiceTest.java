package com.example.lims.service;

import com.example.lims.dto.MagazineDto;
import com.example.lims.enums.MagazineGenre;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.Magazine;
import com.example.lims.repository.MagazineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MagazineServiceTest {

    @Mock
    private MagazineRepository magazineRepository;

    @InjectMocks
    private MagazineService magazineService;

    private Magazine createTestMagazine(UUID id) {
        Magazine mag = new Magazine();
        mag.setTitle("Test Magazine");
        mag.setPublisher("Test Publisher");
        mag.setPublicationDate(LocalDate.now());
        mag.setIssn("1234-5678");
        mag.setLocation("Test Location");
        mag.setLanguage("Russian");
        mag.setGenre(MagazineGenre.POPULAR_SCIENTIFIC);
        mag.setPageCount(100);
        mag.setHasGlossyCover(true);
        return mag;
    }

    @Test
    void create() {
        UUID id = UUID.randomUUID();
        Magazine mag = createTestMagazine(id);
        when(magazineRepository.save(any(Magazine.class))).thenReturn(mag);

        MagazineDto result = magazineService.create(mag);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Magazine", result.getTitle());
        verify(magazineRepository, times(1)).save(mag);
    }

    @Test
    void getById() {
        UUID id = UUID.randomUUID();
        Magazine mag = createTestMagazine(id);
        when(magazineRepository.findById(id)).thenReturn(Optional.of(mag));

        MagazineDto result = magazineService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Magazine", result.getTitle());
    }

    @Test
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(magazineRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> magazineService.getById(id));
    }

    @Test
    void getAll() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Magazine mag1 = createTestMagazine(id1);
        Magazine mag2 = createTestMagazine(id2);
        Page<Magazine> page = new PageImpl<>(List.of(mag1, mag2));

        when(magazineRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<MagazineDto> result = magazineService.getAll(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(id1, result.getContent().get(0).getId());
        assertEquals(id2, result.getContent().get(1).getId());
    }

    @Test
    void search() {
        UUID id = UUID.randomUUID();
        Magazine mag = createTestMagazine(id);
        Page<Magazine> page = new PageImpl<>(List.of(mag));
        when(magazineRepository.searchMagazines(any(), any(), any(), any())).thenReturn(page);

        Page<MagazineDto> result = magazineService.search("Test", MagazineGenre.POPULAR_SCIENTIFIC, "Publisher", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(id, result.getContent().get(0).getId());
    }

    @Test
    void update() {
        UUID id = UUID.randomUUID();
        Magazine existing = createTestMagazine(id);
        Magazine updated = createTestMagazine(id);
        updated.setTitle("Updated Title");

        when(magazineRepository.findById(id)).thenReturn(Optional.of(existing));
        when(magazineRepository.save(any(Magazine.class))).thenReturn(existing);

        MagazineDto result = magazineService.update(id, updated);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(magazineRepository, times(1)).save(existing);
    }

    @Test
    void delete() {
        UUID id = UUID.randomUUID();
        Magazine mag = createTestMagazine(id);
        when(magazineRepository.findById(id)).thenReturn(Optional.of(mag));
        doNothing().when(magazineRepository).delete(mag);

        magazineService.delete(id);

        verify(magazineRepository, times(1)).delete(mag);
    }

    @Test
    void deleteNotFound() {
        UUID id = UUID.randomUUID();
        when(magazineRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> magazineService.delete(id));
    }
}