package com.fulfillx.inventory.event;

import com.fulfillx.inventory.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockReserved(StockReservedEvent event) { // published by inventory and consumed by order
        String messageID = event.getOrderId()+"_"+ UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(messageID);

        log.info(">>> Publishing STOCK_RESERVED for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVENTORY_EXCHANGE,
                RabbitMQConfig.STOCK_RESERVED_ROUTING_KEY,
                event,
                correlationData
        );
//        log.info(">>> STOCK_RESERVED published successfully");
    }

    public void publishStockFailed(StockFailedEvent event) {
        String messageID = event.getOrderId()+"_"+ UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(messageID);
        log.info(">>> Publishing STOCK_FAILED for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVENTORY_EXCHANGE,
                RabbitMQConfig.STOCK_FAILED_ROUTING_KEY,
                event, correlationData
        );
    }
}