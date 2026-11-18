package com.example.lims.service;

import com.example.lims.dto.FineDto;
import com.example.lims.entity.item.ItemCopy;
import com.example.lims.entity.item.LibraryItem;
import com.example.lims.entity.loan.Fine;
import com.example.lims.entity.loan.Loan;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.FineRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FineService {

    private final FineRepository fineRepository;
    private final AuditService auditService;

    public FineService(FineRepository fineRepository, AuditService auditService) {
        this.fineRepository = fineRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<FineDto> getFines(Boolean paid) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<Fine> fines = paid == null
                ? fineRepository.findAll(sort)
                : fineRepository.findByIsPaid(paid, sort);
        return fines.stream().map(this::toDto).toList();
    }

    @Transactional
    public FineDto payFine(UUID fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Штраф не найден"));

        if (Boolean.TRUE.equals(fine.getIsPaid())) {
            throw new BusinessLogicException("Штраф уже отмечен как оплаченный");
        }

        fine.setIsPaid(true);
        Fine saved = fineRepository.save(fine);
        auditService.log("UPDATE", "FINE", fineId);

        return toDto(saved);
    }

    private FineDto toDto(Fine fine) {
        Loan loan = fine.getLoan();
        ItemCopy copy = loan != null ? loan.getCopy() : null;
        LibraryItem item = copy != null ? copy.getItem() : null;

        return new FineDto(
                fine.getId(),
                fine.getAmount(),
                fine.getReason(),
                fine.getIsPaid(),
                loan != null ? loan.getId() : null,
                loan != null && loan.getUser() != null ? loan.getUser().getFullName() : null,
                item != null ? item.getTitle() : null,
                copy != null ? copy.getInventoryNumber() : null,
                loan != null ? loan.getDueDate() : null,
                loan != null ? loan.getReturnDate() : null
        );
    }
}
