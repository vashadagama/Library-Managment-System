package com.example.lims.service;

import com.example.lims.model.Magazine;
import com.example.lims.repository.MagazineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MagazineService {
    private final MagazineRepository magazineRepository;

    public MagazineService(MagazineRepository magazineRepository) {
        this.magazineRepository = magazineRepository;
    }

    public List<Magazine> getAll() {
        return magazineRepository.findAll();
    }

    public Magazine save(Magazine magazine) {
        return magazineRepository.save(magazine);
    }
}