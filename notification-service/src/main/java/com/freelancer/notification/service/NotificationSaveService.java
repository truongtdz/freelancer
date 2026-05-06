package com.freelancer.notification.service;

import com.freelancer.notification.entity.Notification;
import com.freelancer.notification.event.*;
import com.freelancer.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSaveService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void save(NotificationEvent event) {
        if (event.getRecipientUserId() == null) {
            log.warn("Skipping notification with null recipientUserId: {}", event.getClass().getSimpleName());
            return;
        }
        Notification n = new Notification();
        n.setUserId(event.getRecipientUserId());
        n.setReferenceType(event.getReferenceType());
        n.setReferenceId(event.getReferenceId());
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());

        switch (event) {
            case NewApplicationEvent e -> {
                n.setType("NEW_APPLICATION");
                n.setTitle("Ứng tuyển mới");
                n.setContent(e.getFreelancerName() + " đã ứng tuyển vào job \"" + e.getJobTitle() + "\"");
            }
            case ApplicationAcceptedEvent e -> {
                n.setType("APPLICATION_ACCEPTED");
                n.setTitle("Ứng tuyển được chấp nhận");
                n.setContent("Bạn đã được chọn cho job \"" + e.getJobTitle() + "\". Hợp đồng đã được tạo.");
            }
            case PaymentReceivedEvent e -> {
                n.setType("PAYMENT_RECEIVED");
                n.setTitle("Client đã thanh toán");
                n.setContent("Job \"" + e.getJobTitle() + "\" đã được thanh toán. Bạn có thể bắt đầu làm việc.");
            }
            case ProgressReportEvent e -> {
                n.setType("PROGRESS_REPORT");
                n.setTitle("Báo cáo tiến độ mới");
                n.setContent("Freelancer báo cáo tiến độ " + e.getProgressPercentage() + "%");
            }
            case CompletionSubmittedEvent e -> {
                n.setType("COMPLETION_SUBMITTED");
                n.setTitle("Yêu cầu xác nhận hoàn thành");
                n.setContent("Hợp đồng " + e.getContractCode() + " chờ bạn xác nhận.");
            }
            case CompletionConfirmedEvent e -> {
                n.setType("JOB_COMPLETED");
                n.setTitle("Client đã xác nhận");
                n.setContent("Hợp đồng " + e.getContractCode() + " đã được xác nhận. Chờ admin payout.");
            }
            case PayoutCompletedEvent e -> {
                n.setType("PAYOUT_COMPLETED");
                n.setTitle("Đã nhận thanh toán");
                n.setContent("Bạn đã nhận được " + formatVND(e.getNetAmount()) + " từ hợp đồng.");
            }
            case SystemEvent e -> {
                n.setType("SYSTEM");
                n.setTitle(e.getTitle());
                n.setContent(e.getContent());
            }
            default -> {
                log.warn("Unknown event type: {}", event.getClass().getSimpleName());
                return;
            }
        }

        notificationRepository.save(n);
        log.info("Saved notification type={} for user={}", n.getType(), n.getUserId());
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " ₫";
    }
}
