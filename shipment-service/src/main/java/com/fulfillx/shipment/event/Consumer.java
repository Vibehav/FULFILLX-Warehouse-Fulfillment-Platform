package com.fulfillx.shipment.event;

import com.fulfillx.shipment.config.RabbitMQConfig;
import com.fulfillx.shipment.entity.ProcessedOrderConfirmed;
import com.fulfillx.shipment.repository.ProcessedOrderConfirmedRepository;
import com.fulfillx.shipment.service.ShipmentService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class Consumer {

    private final ShipmentService shipmentService;
    private final ProcessedOrderConfirmedRepository processedOrderConfirmedRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRMED_QUEUE)
    public void consumeOrderConfirmed(OrderConfirmedEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG)long deliveryTag) throws IOException {
        log.info(">>> Received ORDER_CONFIRMED for order: {}",
                event.getOrderId());
        try {
            processedOrderConfirmedRepository.save(new ProcessedOrderConfirmed(event.getOrderId()));

            shipmentService.createShipment(event);
            log.info(">>> Shipment created for order: {}",
                    event.getOrderId());
        }catch(DataIntegrityViolationException ignored){

            channel.basicAck(deliveryTag,false);
            log.error("Duplicate Shipment event. Order Id: {} , Exception: {}",event.getOrderId(),ignored.getMessage());

        } catch (Exception e) {
            // If the service retries 3 times and still fails, the exception finally bubbles up to here.
            // We catch it and send it to the Dead Letter Queue.
            log.error(">>> Failed to process Delivery event: {}",e.getMessage());
            channel.basicNack(deliveryTag,false,false);
        }
    }
}