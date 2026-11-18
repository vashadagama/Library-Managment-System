package com.example.lims.service;

import com.example.lims.entity.audit.AuditLog;
import com.example.lims.entity.status.AuditType;
import com.example.lims.entity.user.User;
import com.example.lims.repository.AuditLogRepository;
import com.example.lims.repository.AuditTypeRepository;
import com.example.lims.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Запись действий пользователей в журнал аудита.
 * Ошибка аудита не должна прерывать бизнес-операцию, поэтому логирование
 * выполняется в отдельной транзакции и с подавлением исключений.
 */
@Slf4j
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditTypeRepository auditTypeRepository;

    public AuditService(AuditLogRepository auditLogRepository,
                        AuditTypeRepository auditTypeRepository) {
        this.auditLogRepository = auditLogRepository;
        this.auditTypeRepository = auditTypeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionName, String entityType, UUID entityId) {
        try {
            AuditType type = auditTypeRepository.findByNameIgnoreCase(actionName).orElse(null);
            if (type == null) {
                log.warn("Тип аудита '{}' отсутствует в справочнике audit_types", actionName);
                return;
            }

            AuditLog entry = new AuditLog();
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setAction(type);
            entry.setUser(currentActor());
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Не удалось записать аудит: {} {} / {}", actionName, entityType, ex.getMessage());
        }
    }

    private User currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl details) {
            return details.getUser();
        }
        return null;
    }
}
