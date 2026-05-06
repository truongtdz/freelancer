package com.freelancer.scheduler;

import com.freelancer.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler cho các tác vụ tự động của contract.
 * Sử dụng ShedLock để đảm bảo chỉ 1 node chạy trong môi trường multi-instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractService contractService;

    /**
     * Auto-cancel contract PENDING_PAYMENT quá 24h.
     * Chạy mỗi 15 phút.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000, initialDelay = 60 * 1000)
    @SchedulerLock(
            name = "ContractScheduler_autoCancelExpired",
            lockAtLeastFor = "PT10M",
            lockAtMostFor  = "PT14M"
    )
    public void autoCancelExpiredContracts() {
        log.debug("Running autoCancelExpiredContracts scheduler");
        try {
            contractService.autoCancelExpiredContracts();
        } catch (Exception e) {
            log.error("Error in autoCancelExpiredContracts scheduler", e);
        }
    }

    /**
     * Auto-confirm completion sau 7 ngày CLIENT không xác nhận.
     * Chạy mỗi giờ.
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    @SchedulerLock(
            name = "ContractScheduler_autoConfirmCompletion",
            lockAtLeastFor = "PT30M",
            lockAtMostFor  = "PT59M"
    )
    public void autoConfirmCompletion() {
        log.debug("Running autoConfirmCompletion scheduler");
        try {
            contractService.autoConfirmCompletion();
        } catch (Exception e) {
            log.error("Error in autoConfirmCompletion scheduler", e);
        }
    }
}
