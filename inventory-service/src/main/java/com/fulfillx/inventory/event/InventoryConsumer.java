package com.fulfillx.inventory.event;

import com.fulfillx.inventory.config.RabbitMQConfig;
import com.fulfillx.inventory.entity.ProcessedGrn;
import com.fulfillx.inventory.entity.ProcessedOrder;
import com.fulfillx.inventory.exception.InsufficientStockException;
import com.fulfillx.inventory.repository.ProcessedGrnRepository;
import com.fulfillx.inventory.repository.ProcessedOrderRepository;
import com.fulfillx.inventory.service.InventoryService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventPublisher inventoryEventPublisher;
    private final ProcessedGrnRepository processedGrnRepository;
    private final ProcessedOrderRepository processedOrderRepository;

    //Order Created Event Consumer
    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    @Transactional (rollbackFor = DataIntegrityViolationException.class)
    public void consumeOrderCreated(OrderCreatedEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            // Idempotency check (save to processed_order_ledger)
            processedOrderRepository.save(new ProcessedOrder(event.getOrderId()));

            inventoryService.reserveStockForOrder(event);
        } catch(DataIntegrityViolationException d){

            channel.basicAck(deliveryTag,false);
            log.error("Duplicate Order created not pushed. Order Id: {}",event.getOrderId());
        }catch (InsufficientStockException ie) {

            log.error(">>> Insufficient stock for order: {}", event.getOrderId());
            // Publish stock failed event
            inventoryEventPublisher.publishStockFailed(
                    StockFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .tenantId(event.getTenantId())
                            .reason("Insufficient stock: " + ie.getMessage())
                            .build()
            );

            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            // If the service retries 3 times and still fails, the exception finally bubbles up to here.
            // We catch it and send it to the Dead Letter Queue.
            log.error(">>> Failed to process ORDER CREATED event: {}",e.getMessage());
            channel.basicNack(deliveryTag,false,false);
        }
    }

    // Stock Received Event Consumer
    @RabbitListener(queues = RabbitMQConfig.STOCK_RECEIVED_QUEUE)
    @Transactional // Ensures the ledger insert and stock update commit together
    public void consumeStockReceived(StockReceivedEvent event,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info(">>> Received STOCK_RECEIVED event for GRN: {}", event.getGrnId());

        try {
            // Idempotency check ( saves to processed_grn_ledger)
            processedGrnRepository.save(new ProcessedGrn(event.getGrnId()));

            // business logic
            inventoryService.updateStockFromGRN(event);
            log.info(">>> Stock updated successfully for GRN: {}", event.getGrnId());

            // no requeue i.e. sending successful acknowledgement
            channel.basicAck(deliveryTag,false);
        } catch(DataIntegrityViolationException e) {
            log.warn(" Duplicate Grn detected and Ignored:{}",event.getGrnId());
            channel.basicAck(deliveryTag,false); // remove from the queue, stops same data to enter repeatedly

        } catch (Exception e) {
            // If the service retries 3 times and still fails, the exception finally bubbles up to here.
            // We catch it and send it to the Dead Letter Queue.
            log.error(">>> Failed to process STOCK_RECEIVED event: {}",e.getMessage());
            channel.basicNack(deliveryTag,false,false);
        }
    }
}
