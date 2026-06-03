package com.fulfillx.inbound.event;

import com.fulfillx.inbound.config.RabbitMQConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockReceived(StockReceivedEvent event) {
        String messageID = event.getGrnId()+"_"+ UUID.randomUUID();
        CorrelationData correlationData = new CorrelationData(messageID);

        log.info(" Publishing STOCK_RECEIVED event for GRN: {}, message ID: {}", event.getGrnId(),messageID);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INBOUND_EXCHANGE,
                RabbitMQConfig.STOCK_RECEIVED_ROUTING_KEY,
                event,
                correlationData
        );
//        log.info("STOCK_RECEIVED event published successfully. ");
    }
}