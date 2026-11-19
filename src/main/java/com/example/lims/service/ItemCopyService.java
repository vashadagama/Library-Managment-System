package com.example.lims.service;

import com.example.lims.dto.ItemCopyCreateDto;
import com.example.lims.dto.ItemCopyDto;
import com.example.lims.entity.item.ItemCopy;
import com.example.lims.entity.item.LibraryItem;
import com.example.lims.entity.status.ItemStatus;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.ItemCopyRepository;
import com.example.lims.repository.ItemStatusRepository;
import com.example.lims.repository.LibraryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemCopyService {

    private static final String AVAILABLE = "AVAILABLE";
    private static final String BORROWED = "BORROWED";

    private final ItemCopyRepository itemCopyRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final ItemStatusRepository itemStatusRepository;
    private final AuditService auditService;

    public ItemCopyService(ItemCopyRepository itemCopyRepository,
                           LibraryItemRepository libraryItemRepository,
                           ItemStatusRepository itemStatusRepository,
                           AuditService auditService) {
        this.itemCopyRepository = itemCopyRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.itemStatusRepository = itemStatusRepository;
        this.auditService = auditService;
    }

    public ItemCopyDto createCopy(ItemCopyCreateDto dto) {
        LibraryItem item = libraryItemRepository.findById(dto.getLibraryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Издание не найдено"));

        if (itemCopyRepository.existsByInventoryNumber(dto.getInventoryNumber())) {
            throw new BusinessLogicException("Экземпляр с таким инвентарным номером уже существует");
        }

        ItemCopy copy = new ItemCopy();
        copy.setItem(item);
        copy.setInventoryNumber(dto.getInventoryNumber());
        copy.setAcquisitionDate(dto.getAcquisitionDate());
        copy.setPrice(dto.getPrice());
        copy.setStatus(resolveStatus(AVAILABLE));
        ItemCopy saved = itemCopyRepository.save(copy);
        auditService.log("CREATE", "ITEM_COPY", saved.getId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ItemCopyDto getById(UUID id) {
        return toDto(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ItemCopyDto> getAllCopies() {
        return itemCopyRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ItemCopyDto> getByLibraryItem(UUID libraryItemId) {
        return itemCopyRepository.findByItem_Id(libraryItemId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ItemCopyDto> getByLibraryItemAndStatus(UUID libraryItemId, String statusName) {
        ItemStatus status = resolveStatus(statusName);
        return itemCopyRepository.findByItem_IdAndStatus_Id(libraryItemId, status.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ItemCopyDto> getByStatus(String statusName) {
        ItemStatus status = resolveStatus(statusName);
        return itemCopyRepository.findByStatus_Id(status.getId()).stream().map(this::toDto).toList();
    }

    public ItemCopyDto updateStatus(UUID id, String statusName) {
        ItemCopy copy = getEntity(id);
        ItemStatus newStatus = resolveStatus(statusName);

        if (BORROWED.equalsIgnoreCase(copy.getStatus().getName())
                && AVAILABLE.equalsIgnoreCase(newStatus.getName())) {
            throw new BusinessLogicException("Нельзя вручную установить статус AVAILABLE для выданного экземпляра. Используйте возврат книги.");
        }

        copy.setStatus(newStatus);
        ItemCopy saved = itemCopyRepository.save(copy);
        auditService.log("UPDATE", "ITEM_COPY", saved.getId());
        return toDto(saved);
    }

    public void deleteCopy(UUID id) {
        ItemCopy copy = getEntity(id);
        if (BORROWED.equalsIgnoreCase(copy.getStatus().getName())) {
            throw new BusinessLogicException("Нельзя удалить выданный экземпляр");
        }
        auditService.log("DELETE", "ITEM_COPY", copy.getId());
        itemCopyRepository.delete(copy);
    }

    private ItemCopy getEntity(UUID id) {
        return itemCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Экземпляр не найден"));
    }

    private ItemStatus resolveStatus(String statusName) {
        return itemStatusRepository.findByNameIgnoreCase(statusName)
                .orElseThrow(() -> new ResourceNotFoundException("Статус экземпляра не найден: " + statusName));
    }

    private ItemCopyDto toDto(ItemCopy copy) {
        LibraryItem item = copy.getItem();
        return new ItemCopyDto(
                copy.getId(),
                copy.getInventoryNumber(),
                copy.getStatus() != null ? copy.getStatus().getId() : null,
                copy.getStatus() != null ? copy.getStatus().getName() : null,
                item != null ? item.getId() : null,
                item != null ? item.getTitle() : null,
                copy.getAcquisitionDate(),
                copy.getPrice()
        );
    }
}
