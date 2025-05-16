package com.autohub.user_service.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ message queue.
 * Defines queues, exchanges, and bindings for asynchronous processing of non-critical operations.
 */
@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String EMAIL_QUEUE = "email-queue";
    public static final String AUDIT_LOG_QUEUE = "audit-log-queue";
    public static final String NOTIFICATION_QUEUE = "notification-queue";
    
    // Exchange name
    public static final String USER_SERVICE_EXCHANGE = "user-service-exchange";
    
    // Routing keys
    public static final String EMAIL_ROUTING_KEY = "email";
    public static final String AUDIT_LOG_ROUTING_KEY = "audit.log";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EMAIL_QUEUE + ".dlq")
                .build();
    }
    
    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE + ".dlq").build();
    }
    
    @Bean
    public Queue auditLogQueue() {
        return QueueBuilder.durable(AUDIT_LOG_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", AUDIT_LOG_QUEUE + ".dlq")
                .build();
    }
    
    @Bean
    public Queue auditLogDeadLetterQueue() {
        return QueueBuilder.durable(AUDIT_LOG_QUEUE + ".dlq").build();
    }
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_QUEUE + ".dlq")
                .build();
    }
    
    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE + ".dlq").build();
    }
    
    @Bean
    public TopicExchange userServiceExchange() {
        return new TopicExchange(USER_SERVICE_EXCHANGE);
    }
    
    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange userServiceExchange) {
        return BindingBuilder.bind(emailQueue).to(userServiceExchange).with(EMAIL_ROUTING_KEY);
    }
    
    @Bean
    public Binding auditLogBinding(Queue auditLogQueue, TopicExchange userServiceExchange) {
        return BindingBuilder.bind(auditLogQueue).to(userServiceExchange).with(AUDIT_LOG_ROUTING_KEY);
    }
    
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange userServiceExchange) {
        return BindingBuilder.bind(notificationQueue).to(userServiceExchange).with(NOTIFICATION_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
