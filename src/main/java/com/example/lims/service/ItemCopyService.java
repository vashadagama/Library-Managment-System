package com.example.lims.service;

import com.example.lims.dto.ItemCopyCreateDto;
import com.example.lims.enums.ItemStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.model.ItemCopy;
import com.example.lims.model.LibraryItem;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.LibraryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemCopyService {

    private final ItemCopyRepository itemCopyRepository;
    private final LibraryItemRepository libraryItemRepository;

    public ItemCopyService(ItemCopyRepository itemCopyRepository, LibraryItemRepository libraryItemRepository) {
        this.itemCopyRepository = itemCopyRepository;
        this.libraryItemRepository = libraryItemRepository;
    }

    public ItemCopy createCopy(ItemCopyCreateDto dto) {
        LibraryItem item = libraryItemRepository.findById(dto.getLibraryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Издание не найдено"));

        if (itemCopyRepository.existsByInventoryNumber(dto.getInventoryNumber())) {
            throw new BusinessLogicException("Экземпляр с таким инвентарным номером уже существует");
        }

        ItemCopy copy = new ItemCopy(item, dto.getInventoryNumber());
        copy.setStatus(ItemStatus.AVAILABLE);
        return itemCopyRepository.save(copy);
    }

    @Transactional(readOnly = true)
    public ItemCopy getById(UUID id) {
        return itemCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Экземпляр не найден"));
    }

    @Transactional(readOnly = true)
    public List<ItemCopy> getAllCopies() {
        return itemCopyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ItemCopy> getByLibraryItem(UUID libraryItemId) {
        return itemCopyRepository.findByItemId(libraryItemId);
    }

    @Transactional(readOnly = true)
    public List<ItemCopy> getByLibraryItemAndStatus(UUID libraryItemId, ItemStatus status) {
        return itemCopyRepository.findByItemIdAndStatus(libraryItemId, status);
    }

    @Transactional(readOnly = true)
    public List<ItemCopy> getByStatus(ItemStatus status) {
        return itemCopyRepository.findByStatus(status);
    }

    public ItemCopy updateStatus(UUID id, ItemStatus newStatus) {
        ItemCopy copy = getById(id);

        if (copy.getStatus() == ItemStatus.BORROVED && newStatus != ItemStatus.AVAILABLE) {
            throw new BusinessLogicException("Нельзя изменить статус выданного экземпляра, кроме как на AVAILABLE (возврат)");
        }

        copy.setStatus(newStatus);
        return itemCopyRepository.save(copy);
    }

    public void deleteCopy(UUID id) {
        ItemCopy copy = getById(id);
        if (copy.getStatus() == ItemStatus.BORROVED) {
            throw new BusinessLogicException("Нельзя удалить выданный экземпляр");
        }
        itemCopyRepository.delete(copy);
    }

    @Transactional(readOnly = true)
    public List<ItemCopy> getAvailableCopies(UUID itemId) {
        return itemCopyRepository.findByItemIdAndStatus(itemId, ItemStatus.AVAILABLE);
    }
}