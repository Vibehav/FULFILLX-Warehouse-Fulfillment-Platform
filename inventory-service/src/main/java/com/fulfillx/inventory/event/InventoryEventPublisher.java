package com.fulfillx.inventory.event;

import com.fulfillx.inventory.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockReserved(StockReservedEvent event) {
        log.info(">>> Publishing STOCK_RESERVED for order: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVENTORY_EXCHANGE,
                RabbitMQConfig.STOCK_RESERVED_ROUTING_KEY,
                event
        );
        log.info(">>> STOCK_RESERVED published successfully");
    }

}