package com.example.lims.service;

import com.example.lims.enums.ItemStatus;
import com.example.lims.model.ItemCopy;
import com.example.lims.repository.ItemCopyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemCopyService {
    private final ItemCopyRepository itemCopyRepository;

    public ItemCopyService(ItemCopyRepository itemCopyRepository) {
        this.itemCopyRepository = itemCopyRepository;
    }

    public List<ItemCopy> getAvailableCopies(UUID itemId) {
        return itemCopyRepository.findByItemIdAndStatus(itemId, ItemStatus.AVAILABLE);
    }
}