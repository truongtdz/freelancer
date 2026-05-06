package com.freelancer.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "freelancer.exchange";
    public static final String QUEUE    = "notification.queue";
    public static final String ROUTING  = "notification.#";

    @Bean
    public TopicExchange freelancerExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange freelancerExchange) {
        return BindingBuilder.bind(notificationQueue).to(freelancerExchange).with(ROUTING);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jsonConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // Dùng SimpleMessageConverter để nhận raw bytes — consumer tự deserialize bằng Jackson
        factory.setMessageConverter(new SimpleMessageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
