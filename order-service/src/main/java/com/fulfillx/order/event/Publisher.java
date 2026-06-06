package com.fulfillx.order.event;

import com.fulfillx.order.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class Publisher {

    private final RabbitTemplate rabbitTemplate;

    // Order Created Event
    public void publishOrderCreated(OrderCreatedEvent event) {

        String messageID = event.getOrderId() + " " + UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(messageID);
        log.info(">>> Publishing ORDER_CREATED for order: {}",
                event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                event,
                correlationData
        );
        log.info(">>> ORDER_CREATED published successfully");
    }

    // Order Confirmed Event
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        String messageID = event.getOrderId() + " " + UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(messageID);
        log.info(">>> Publishing ORDER_CONFIRMED for order: {}",
                event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CONFIRMED_ROUTING_KEY,
                event, correlationData
        );
        log.info(">>> ORDER_CONFIRMED published successfully");
    }
}