package com.example.lims.service;

import com.example.lims.dto.NotificationDto;
import com.example.lims.entity.item.LibraryItem;
import com.example.lims.entity.loan.Loan;
import com.example.lims.entity.notification.Notification;
import com.example.lims.entity.status.NotificationType;
import com.example.lims.repository.LoanRepository;
import com.example.lims.repository.NotificationRepository;
import com.example.lims.repository.NotificationTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Генерация уведомлений читателям: DUE_SOON (скоро срок возврата)
 * и OVERDUE (просрочка). Вызывается планировщиком и при старте приложения.
 * Повторные уведомления по одной и той же ситуации не создаются
 * (дедупликация по пользователю, типу и тексту сообщения).
 */
@Slf4j
@Service
public class NotificationService {

    private static final int DUE_SOON_WINDOW_DAYS = 3;
    private static final List<String> ACTIVE_STATUSES = List.of("ACTIVE", "RENEWED");

    private final LoanRepository loanRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;

    public NotificationService(LoanRepository loanRepository,
                               NotificationRepository notificationRepository,
                               NotificationTypeRepository notificationTypeRepository) {
        this.loanRepository = loanRepository;
        this.notificationRepository = notificationRepository;
        this.notificationTypeRepository = notificationTypeRepository;
    }

    @Transactional
    public int generateScheduledNotifications() {
        LocalDate today = LocalDate.now();
        int created = 0;

        created += generateDueSoon(today);
        created += generateOverdue(today);

        if (created > 0) {
            log.info("Создано уведомлений: {} (DUE_SOON/OVERDUE)", created);
        }
        return created;
    }

    private int generateDueSoon(LocalDate today) {
        NotificationType type = notificationTypeRepository.findByNameIgnoreCase("DUE_SOON").orElse(null);
        if (type == null) {
            return 0;
        }

        LocalDate until = today.plusDays(DUE_SOON_WINDOW_DAYS);
        List<Loan> loans = loanRepository.findDueBetween(ACTIVE_STATUSES, today, until);

        int created = 0;
        for (Loan loan : loans) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, loan.getDueDate());
            String message = String.format(
                    "Верните издание «%s» до %s. Осталось %d дн.",
                    titleOf(loan), loan.getDueDate(), Math.max(daysLeft, 0));
            created += createIfAbsent(loan, type, message);
        }
        return created;
    }

    private int generateOverdue(LocalDate today) {
        NotificationType type = notificationTypeRepository.findByNameIgnoreCase("OVERDUE").orElse(null);
        if (type == null) {
            return 0;
        }

        List<Loan> loans = loanRepository.findByStatusNames(List.of("OVERDUE"));

        int created = 0;
        for (Loan loan : loans) {
            String message = String.format(
                    "Просрочка: издание «%s» не возвращено (срок истёк %s). Начислен штраф.",
                    titleOf(loan), loan.getDueDate());
            created += createIfAbsent(loan, type, message);
        }
        return created;
    }

    private int createIfAbsent(Loan loan, NotificationType type, String message) {
        boolean exists = notificationRepository.existsByUserIdAndTypeIdAndMessage(
                loan.getUser().getId(), type.getId(), message);
        if (exists) {
            return 0;
        }

        Notification notification = new Notification();
        notification.setType(type);
        notification.setUser(loan.getUser());
        notification.setMessage(message);
        notificationRepository.save(notification);
        return 1;
    }

    private String titleOf(Loan loan) {
        LibraryItem item = loan.getCopy() != null ? loan.getCopy().getItem() : null;
        return item != null && item.getTitle() != null ? item.getTitle() : "неизвестное издание";
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getLatest() {
        return notificationRepository.findTop50ByOrderBySentAtDesc().stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getUser() != null ? n.getUser().getEmail() : null,
                        n.getType() != null ? n.getType().getName() : null,
                        n.getMessage(),
                        n.getSentAt()))
                .toList();
    }
}
