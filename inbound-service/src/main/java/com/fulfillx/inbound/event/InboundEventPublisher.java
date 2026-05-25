package com.fulfillx.inbound.event;

import com.fulfillx.inbound.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockReceived(StockReceivedEvent event) {
        log.info(" Publishing STOCK_RECEIVED event for GRN: {}", event.getGrnId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INBOUND_EXCHANGE,
                RabbitMQConfig.STOCK_RECEIVED_ROUTING_KEY,
                event
        );
        log.info("STOCK_RECEIVED event published successfully. ");
    }
}