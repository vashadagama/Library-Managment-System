package com.example.lims.service;

import com.example.lims.enums.MagazineGenre;
import com.example.lims.model.Magazine;
import com.example.lims.repository.MagazineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MagazineService {
    private final MagazineRepository magazineRepository;

    public MagazineService(MagazineRepository magazineRepository) {
        this.magazineRepository = magazineRepository;
    }

    public Page<Magazine> getAll(int page, int size) {
        return magazineRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Magazine> search(MagazineGenre genre, String publisher, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return magazineRepository.searchMagazines(genre, publisher, pageable);
    }

    public Magazine save(Magazine magazine) {
        return magazineRepository.save(magazine);
    }
}