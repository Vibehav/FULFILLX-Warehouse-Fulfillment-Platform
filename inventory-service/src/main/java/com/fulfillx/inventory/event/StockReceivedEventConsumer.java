package com.fulfillx.inventory.event;

import com.fulfillx.inventory.config.RabbitMQConfig;
import com.fulfillx.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReceivedEventConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_RECEIVED_QUEUE)
    public void consumeStockReceived(StockReceivedEvent event) {
        log.info(">>> Received STOCK_RECEIVED event for GRN: {}", event.getGrnId());

        try {
            inventoryService.updateStockFromGRN(event);
            log.info(">>> Stock updated successfully for GRN: {}", event.getGrnId());
        } catch (Exception e) {
            log.error(">>> Failed to process STOCK_RECEIVED event: {}",
                    e.getMessage());
            throw e;
        }
    }
}