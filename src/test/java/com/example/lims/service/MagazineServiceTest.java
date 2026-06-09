package com.example.lims.service;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MagazineServiceTest {

    @Mock
    private MagazineRepository magazineRepository;

    @InjectMocks
    private MagazineService magazineService;

    @Test
    void create() {
        Magazine mag = new Magazine();
        when(magazineRepository.save(mag)).thenReturn(mag);

        Magazine result = magazineService.create(mag);

        assertNotNull(result);
    }

    @Test
    void getById() {
        UUID id = UUID.randomUUID();
        Magazine mag = new Magazine();
        when(magazineRepository.findById(id)).thenReturn(Optional.of(mag));

        assertNotNull(magazineService.getById(id));
    }

    @Test
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(magazineRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> magazineService.getById(id));
    }
}