package com.fulfillx.shipment.event;

import com.fulfillx.shipment.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.ast.tree.from.CorrelatedPluralTableGroup;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderDelivered(OrderDeliveredEvent event) {

        String messageId = event.getOrderId() + " " + UUID.randomUUID();
        CorrelationData correlationData  = new CorrelationData(messageId);
        log.info(">>> Publishing ORDER_DELIVERED for order: {}",
                event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SHIPMENT_EXCHANGE,
                RabbitMQConfig.ORDER_DELIVERED_ROUTING_KEY,
                event, correlationData
        );
        log.info(">>> ORDER_DELIVERED published successfully");
    }
}