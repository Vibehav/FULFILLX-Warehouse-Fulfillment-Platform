package com.fulfillx.order.event;

import com.fulfillx.order.config.RabbitMQConfig;
import com.fulfillx.order.entity.ProcessedInsufficientStock;
import com.fulfillx.order.entity.ProcessedOrderDeliver;
import com.fulfillx.order.entity.ProcessedReservedStock;
import com.fulfillx.order.repository.ProcessedInsufficientStockRepository;
import com.fulfillx.order.repository.ProcessedOrderDeliveryRepository;
import com.fulfillx.order.repository.ProcessedReservedStockRepository;
import com.fulfillx.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.Header;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class Consumer {

    private final OrderService orderService;
    private final ProcessedOrderDeliveryRepository processedOrderDeliveryRepository;
    private final ProcessedInsufficientStockRepository processedInsufficientStockRepository;
    private final ProcessedReservedStockRepository processedReservedStockRepository;

    // Order Delivered Event
    @RabbitListener(queues = RabbitMQConfig.ORDER_DELIVERED_QUEUE)
    @Transactional
    public void consumeOrderDelivered(OrderDeliveredEvent event,
                                      Channel channel,@Header(AmqpHeaders.DELIVERY_TAG)long deliveryTag) throws IOException {

        log.info(">>> Received ORDER_DELIVERED for order: {}",
                event.getOrderId());
        try {
            // Idempotency check (saved to PROCESSED_ORDER_LEDGER)
            processedOrderDeliveryRepository.save(new ProcessedOrderDeliver(event.getOrderId()));
            orderService.deliverOrder(event);
            log.info(">>> Order marked delivered: {}", event.getOrderId());
        } catch(DataIntegrityViolationException ignored){

            log.error("Duplicate Order Delivery log. Order Id: {} , Exception: {}",event.getOrderId(),ignored.getMessage());
            channel.basicAck(deliveryTag,false);

        } catch (Exception e) {
            // If the service retries 3 times and still fails, the exception finally bubbles up to here.
            // We catch it and send it to the Dead Letter Queue.
            log.error(">>> Failed to process ORDER Delivered event: {}",e.getMessage());
            channel.basicNack(deliveryTag,false,false);
        }
    }

    // Order Failed Event
    @RabbitListener(queues = RabbitMQConfig.STOCK_FAILED_QUEUE)
    @Transactional
    public void consumeStockFailed(StockFailedEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info(">>> Received STOCK_FAILED for order: {}", event.getOrderId());
        try {
            // Idempotency
            processedInsufficientStockRepository.save(new ProcessedInsufficientStock(event.getOrderId()));

            // fail order service
            orderService.failOrder(event);
        } catch(DataIntegrityViolationException ignored){

            channel.basicAck(deliveryTag,false);
            log.error("Duplicate stock failed event. Order Id: {} , Exception: {}",event.getOrderId(),ignored.getMessage());

        } catch (Exception e) {
            // If the service retries 3 times and still fails, the exception finally bubbles up to here.
            // We catch it and send it to the Dead Letter Queue.
            log.error(">>> Failed to process Stock failed event: {}",e.getMessage());
            channel.basicNack(deliveryTag,false,false);
        }
    }


    // Stock Reserved Event
    @RabbitListener(queues = RabbitMQConfig.STOCK_RESERVED_QUEUE)
    @Transactional
    public void consumeStockReserved(StockReservedEvent event, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info(">>> Received STOCK_RESERVED for order: {}",
                event.getOrderId());
        try {
            processedReservedStockRepository.save(new ProcessedReservedStock(event.getOrderId()));

            orderService.confirmOrder(event);
            log.info(">>> Order confirmed: {}", event.getOrderId());
        } catch(DataIntegrityViolationException ignored){

            channel.basicAck(deliveryTag,false);
            log.error("Duplicate Stock reserved event. Order Id: {} , Exception: {}",event.getOrderId(),ignored.getMessage());

        } catch (Exception e) {
            // If the service retries 3 times and still fails, the exception finally bubbles up to here.
            // We catch it and send it to the Dead Letter Queue.
            log.error(">>> Failed to process Stock reserved event: {}",e.getMessage());
            channel.basicNack(deliveryTag,false,false);
        }
    }
}
