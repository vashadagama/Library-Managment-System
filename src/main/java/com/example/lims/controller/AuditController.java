package com.example.lims.controller;

import com.example.lims.dto.AuditDto;
import com.example.lims.entity.audit.AuditLog;
import com.example.lims.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public Page<AuditDto> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return auditLogRepository.findAllByOrderByChangedAtDesc(PageRequest.of(page, size))
                .map(this::toDto);
    }

    private AuditDto toDto(AuditLog entry) {
        return new AuditDto(
                entry.getId(),
                entry.getAction() != null ? entry.getAction().getName() : null,
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getUser() != null ? entry.getUser().getEmail() : null,
                entry.getChangedAt()
        );
    }
}
