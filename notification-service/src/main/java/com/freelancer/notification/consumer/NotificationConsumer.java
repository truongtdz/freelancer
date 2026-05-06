package com.freelancer.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.notification.event.NotificationEvent;
import com.freelancer.notification.service.NotificationSaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationSaveService saveService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.queue")
    public void consume(Message rawMessage) {
        try {
            String json = new String(rawMessage.getBody(), StandardCharsets.UTF_8);
            // Dùng @JsonTypeInfo trong NotificationEvent để resolve đúng subclass
            NotificationEvent event = objectMapper.readValue(json, NotificationEvent.class);
            log.debug("Received event: {} for user={}", event.getClass().getSimpleName(), event.getRecipientUserId());
            saveService.save(event);
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Processing failed", e);
        }
    }
}
