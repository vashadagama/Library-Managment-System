package com.example.lims.scheduler;

import com.example.lims.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OverdueScheduler.class);

    private final LoanService loanService;

    public OverdueScheduler(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void markOverdueLoans() {
        int changed = loanService.markOverdueLoans();
        if (changed > 0) {
            logger.info("Обновлён статус OVERDUE для {} выдач.", changed);
        }
    }
}
