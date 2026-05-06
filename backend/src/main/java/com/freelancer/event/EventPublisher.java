package com.freelancer.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:freelancer.exchange}")
    private String exchange;

    /**
     * Publishes an event to RabbitMQ.
     * Swallows all exceptions — notifications are nice-to-have and MUST NOT block business operations.
     */
    public void publish(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.debug("Published {} to exchange={} routingKey={}", event.getClass().getSimpleName(), exchange, routingKey);
        } catch (Exception e) {
            log.error("Publish event failed — swallowing error (notification is non-critical): {}", e.getMessage(), e);
            // Intentionally NOT re-throwing — business operations must not be blocked by notification failures
        }
    }
}
