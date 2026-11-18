package com.example.lims.scheduler;

import com.example.lims.service.LoanService;
import com.example.lims.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Ежедневная отметка просроченных выдач (03:00) и генерация уведомлений.
 * Catch-up при старте: если приложение не работало в момент запуска задания,
 * статусы и уведомления приводятся в актуальное состояние после поднятия.
 */
@Component
public class OverdueScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OverdueScheduler.class);

    private final LoanService loanService;
    private final NotificationService notificationService;

    public OverdueScheduler(LoanService loanService, NotificationService notificationService) {
        this.loanService = loanService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledRun() {
        run("по расписанию");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpOnStartup() {
        run("catch-up при старте");
    }

    private void run(String origin) {
        try {
            int changed = loanService.markOverdueLoans();
            int notifications = notificationService.generateScheduledNotifications();
            if (changed > 0 || notifications > 0) {
                logger.info("Планировщик ({}): OVERDUE у {} выдач, создано уведомлений: {}.",
                        origin, changed, notifications);
            }
        } catch (Exception ex) {
            logger.error("Ошибка выполнения планировщика ({}): {}", origin, ex.getMessage(), ex);
        }
    }
}
