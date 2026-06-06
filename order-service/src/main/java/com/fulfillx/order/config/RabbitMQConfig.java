package com.fulfillx.order.config;

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
    public static final String ORDER_CREATED_QUEUE = "order.created.queue"; // this
    public static final String ORDER_CONFIRMED_QUEUE = "order.confirmed.queue"; // this
    public static final String STOCK_RESERVED_QUEUE = "stock.reserved.queue"; // inventory
    public static final String STOCK_FAILED_QUEUE = "stock.failed.queue"; // inventory
    public static final String ORDER_DELIVERED_QUEUE = "order.delivered.queue"; // shipment
    // DLQ Queues
    public static final String ORDER_CREATED_DLQ = "order.created.dlq"; // this
    public static final String ORDER_CONFIRMED_DLQ = "order.confirmed.dlq"; // this
    public static final String STOCK_RESERVED_DLQ = "stock.reserved.dlq"; // inventory
    public static final String STOCK_FAILED_DLQ = "stock.failed.dlq"; // inventory
    public static final String ORDER_DELIVERED_DLQ = "order.delivered.dlq"; // shipment


    // ===== Exchanges =====
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String SHIPMENT_EXCHANGE = "shipment.exchange";
    // ===== DLX Exchanges =====
    public static final String ORDER_CREATED_DLX = "order.created.dlx";
    public static final String ORDER_CONFIRMED_DLX = "order.confirmed.dlx";
    public static final String STOCK_RESERVED_DLX = "stock.reserved.dlx";
    public static final String STOCK_FAILED_DLX = "stock.failed.dlx";
    public static final String SHIPMENT_EXCHANGE_DLX = "shipment.exchange.dlx";


    // ===== Routing Keys =====
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created.key";
    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved.key";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed.key";
    public static final String ORDER_DELIVERED_ROUTING_KEY = "order.delivered.key";
    public static final String STOCK_FAILED_ROUTING_KEY = "stock.failed.key";
    // ===== DLQ Routing Keys =====
    public static final String ORDER_CREATED_DLQ_KEY = "order.created.dlq.key";
    public static final String ORDER_CONFIRMED_DLQ_KEY = "order.confirmed.dlq.key";
    public static final String STOCK_RESERVED_DLQ_KEY = "stock.reserved.dlq.key";
    public static final String STOCK_FAILED_DLQ_KEY = "stock.failed.dlq.key";
    public static final String ORDER_DELIVERED_DLQ_KEY = "order.delivered.dlq.key";

    // ======================================================================
    // ORDER CREATED EVENT
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .quorum()
                .withArgument("x-dead-letter-exchange", ORDER_CREATED_DLX)
                .withArgument("x-dead-letter-routing-key",ORDER_CREATED_DLQ_KEY)
                .build();
    }
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }
    // DLQ
    @Bean
    public Queue orderCreatedDLQ(){ return QueueBuilder.durable(ORDER_CREATED_DLQ).quorum().build(); }
    @Bean
    public DirectExchange orderCreatedDLX(){ return new DirectExchange(ORDER_CREATED_DLX); }
    @Bean Binding orderCreatedDLBinding(){
        return BindingBuilder
                .bind(orderCreatedDLQ())
                .to(orderCreatedDLX())
                .with(ORDER_CREATED_DLQ_KEY);
    }
    // =====================================================================

    // =====================================================================
    // Order Confirmed Queue
    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(ORDER_CONFIRMED_QUEUE)
                .quorum()
                .withArgument("x-dead-letter-exchange",ORDER_CONFIRMED_DLX)
                .withArgument("x-dead-letter-routing-key",ORDER_CONFIRMED_DLQ_KEY)
                .build();
    }
    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder
                .bind(orderConfirmedQueue())
                .to(orderExchange())
                .with(ORDER_CONFIRMED_ROUTING_KEY);
    }
    @Bean
    public Queue orderConfirmedDLQ(){ return QueueBuilder.durable(ORDER_CONFIRMED_DLQ).quorum()
            .build(); }
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

    // ====================================================
    // Stock Reserved Event
    @Bean
    public Queue stockReservedQueue() {
        return QueueBuilder.durable(STOCK_RESERVED_QUEUE)
                .quorum()
                .withArgument("x-dead-letter-exchange",STOCK_RESERVED_DLX)
                .withArgument("x-dead-letter-routing-key",STOCK_RESERVED_DLQ_KEY)
                .build();
    }
    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder
                .bind(stockReservedQueue())
                .to(inventoryExchange())
                .with(STOCK_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Queue stockReservedDLQ(){
        return QueueBuilder.durable(STOCK_RESERVED_DLQ).quorum().build();
    }
    @Bean
    public DirectExchange stockReservedDLX(){
        return new DirectExchange(STOCK_RESERVED_DLX);
    }
    @Bean
    public Binding stockReservedDLQBinding(){
        return BindingBuilder
                .bind(stockReservedDLQ())
                .to(stockReservedDLX())
                .with(STOCK_RESERVED_DLQ_KEY);

    }
    // ====================================================


    // ====================================================
    // Stock Failed Event
    @Bean
    public Queue stockFailedQueue() {
        return QueueBuilder.durable(STOCK_FAILED_QUEUE)
                .quorum()
                .withArgument("x-dead-letter-exchange",STOCK_FAILED_DLX)
                .withArgument("x-dead-letter-routing-key",STOCK_FAILED_DLQ_KEY)
                .build();
    }
    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE);
    }
    @Bean
    public Binding stockFailedBinding() {
        return BindingBuilder
                .bind(stockFailedQueue())
                .to(inventoryExchange())
                .with(STOCK_FAILED_ROUTING_KEY);
    }
    @Bean
    public Queue stockFailedDLQ() {
        return QueueBuilder.durable(STOCK_FAILED_DLQ).quorum().build();
    }
    @Bean
    public DirectExchange stockFailedDLX(){ return new DirectExchange(STOCK_FAILED_DLX); }
    @Bean
    public Binding stockFailedDLQBinding(){
        return BindingBuilder
                .bind(stockFailedDLQ())
                .to(stockFailedDLX())
                .with(STOCK_FAILED_DLQ_KEY);
    }
    // ====================================================

    // ===== Message Converter =====
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ===== RabbitTemplate =====
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