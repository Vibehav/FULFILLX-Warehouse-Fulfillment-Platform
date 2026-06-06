package com.fulfillx.shipment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    // ===== Queues =====
    public static final String ORDER_CONFIRMED_QUEUE = "order.confirmed.queue";
    public static final String ORDER_DELIVERED_QUEUE = "order.delivered.queue";
    // DLQs
    public static final String ORDER_CONFIRMED_DLQ = "order.confirmed.dlq"; // this
    public static final String ORDER_DELIVERED_DLQ = "order.delivered.dlq";


    // ===== Exchanges =====
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String SHIPMENT_EXCHANGE = "shipment.exchange";
    // DLXs
    public static final String ORDER_CONFIRMED_DLX = "order.confirmed.dlx";
    public static final String SHIPMENT_EXCHANGE_DLX = "shipment.exchange.dlx";

    // ===== Routing Keys =====
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String ORDER_DELIVERED_ROUTING_KEY = "order.delivered";
    // DLKs
    public static final String ORDER_CONFIRMED_DLQ_KEY = "order.confirmed.dlq.key";
    public static final String ORDER_DELIVERED_DLQ_KEY = "order.delivered.dlq.key";

    // =====================================================================
    // ORDER DELIVERED EVENT
    @Bean
    public Queue orderDeliveredQueue() {
        return QueueBuilder.durable(ORDER_DELIVERED_QUEUE)
                .quorum()
                .withArgument("x-dead-letter-exchange",SHIPMENT_EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key",ORDER_DELIVERED_DLQ_KEY)
                .build();
    }

    @Bean
    public TopicExchange shipmentExchange() {
        return new TopicExchange(SHIPMENT_EXCHANGE);
    }

    @Bean
    public Binding orderDeliveredBinding() {
        return BindingBuilder
                .bind(orderDeliveredQueue())
                .to(shipmentExchange())
                .with(ORDER_DELIVERED_ROUTING_KEY);
    }

    @Bean
    public Queue orderDeliveredQueueDLQ() { return QueueBuilder.durable(ORDER_DELIVERED_DLQ).quorum().build(); }

    @Bean
    public DirectExchange orderDeliveredDLX() {
        return new DirectExchange(SHIPMENT_EXCHANGE_DLX);
    }

    @Bean
    public Binding orderDeliveredDLBinding(){
        return BindingBuilder.bind(orderDeliveredQueueDLQ())
                .to(orderDeliveredDLX())
                .with(ORDER_DELIVERED_DLQ_KEY);
    }

    // =====================================================================
    // ORDER CONFIRMED EVENT
    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(ORDER_CONFIRMED_QUEUE).quorum()
                .withArgument("x-dead-letter-exchange",ORDER_CONFIRMED_DLX)
                .withArgument("x-dead-letter-routing-key",ORDER_CONFIRMED_DLQ_KEY)
                .build();
    }
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder
                .bind(orderConfirmedQueue())
                .to(orderExchange())
                .with(ORDER_CONFIRMED_ROUTING_KEY);
    }
    @Bean
    public Queue orderConfirmedDLQ(){ return QueueBuilder.durable(ORDER_CONFIRMED_DLQ).quorum().build(); }
    @Bean
    public DirectExchange orderConfirmedDLX() {
        return new DirectExchange(ORDER_CONFIRMED_DLX);
    }
    @Bean
    public Binding orderConfirmedDLBinding() {
        return BindingBuilder
                .bind(orderConfirmedDLQ())
                .to(orderConfirmedDLX())
                .with(ORDER_CONFIRMED_ROUTING_KEY);
    }
    // =====================================================================

    // Message Converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Rabbit Template
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true); //  publisher-returns: true in yml
        // without this, unroutable messages are silently dropped instead of returned.

        // Set callbacks here, not in @PostConstruct because what if messages are sent before the bean is created
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            String messageId = correlationData.getId();
            if (ack) {
                log.info("RabbitMQ confirmed receipt of message ID: {}", messageId);
            } else {
                log.error("RabbitMQ Rejected message ID: {}, Reason: {}", messageId, cause);
            }

            // future work: save to outbox table in db for cron job
        });

        template.setReturnsCallback(returned -> {
            // you have publisher-returns: true but no ReturnsCallback — add this too
            log.error("Message returned: exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText());
        });

        return template;
    }
}