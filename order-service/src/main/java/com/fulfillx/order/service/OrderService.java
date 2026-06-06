package com.fulfillx.order.service;

import com.fulfillx.order.dto.*;
import com.fulfillx.order.entity.Order;
import com.fulfillx.order.entity.OrderItem;
import com.fulfillx.order.enums.OrderItemStatus;
import com.fulfillx.order.enums.OrderStatus;
import com.fulfillx.order.event.*;
import com.fulfillx.order.exception.*;
import com.fulfillx.order.repository.OrderItemRepository;
import com.fulfillx.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final Publisher eventPublisher;

    // ===== Create Order =====
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String sellerId, String tenantId) {
        // Create order
        Order order = Order.builder()
                .sellerId(sellerId)
                .tenantId(tenantId)
                .warehouseId(request.getWarehouseId())
                .remarks(request.getRemarks())
                .build();
        // save in db
        Order savedOrder = orderRepository.save(order);

        // passing the ordered items from the stream
        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> OrderItem.builder()
                        .order(savedOrder)
                        .skuId(itemRequest.getSkuId())
                        .quantity(itemRequest.getQuantity())
                        .warehouseId(itemRequest.getWarehouseId())
                        .build()
                ).collect(Collectors.toList());

        orderItemRepository.saveAll(items); // save in order item repo
        savedOrder.setItems(items); // link list of items to orders table

        // Publish ORDER_CREATED event (This event will get Consumed by Inventory Service)
        eventPublisher.publishOrderCreated(
                OrderCreatedEvent.builder()
                        .orderId(savedOrder.getId())
                        .tenantId(tenantId)
                        .warehouseId(request.getWarehouseId())
                        .items(items.stream()
                                .map(item -> OrderCreatedEvent.OrderItem.builder()
                                        .skuId(item.getSkuId())
                                        .quantity(item.getQuantity())
                                        .warehouseId(item.getWarehouseId())
                                        .build()
                                ).collect(Collectors.toList()))
                        .build());

        log.info(">>> Order created: {}", savedOrder.getId());
        return mapToResponse(savedOrder);
    }

    // Confirm Order (called by RabbitMQ consumer)
    @Transactional
    @Retryable( // Only retry for temporary database locks/concurrency issues
            retryFor = { ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 150, multiplier = 2)
    )
    public void confirmOrder(StockReservedEvent event) {

        Order order = orderRepository.findById(event.getOrderId()).orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.getOrderId()));

        // check the state if it's not pending, it might have cancelled,shipped,delivered,failed,confirmed
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn(">>> Order {} is not in PENDING state", event.getOrderId());
            return;
        }

        // Update order items as RESERVED
        List<OrderItem> items = orderItemRepository.findByOrderId(event.getOrderId());

        items.forEach(item -> item.setStatus(OrderItemStatus.RESERVED));
        orderItemRepository.saveAll(items);


        // Update order status
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Publish ORDER CONFIRMED event (consumed by order service through rabbitmq)
        eventPublisher.publishOrderConfirmed(
                OrderConfirmedEvent.builder()
                        .orderId(order.getId())
                        .tenantId(order.getTenantId())
                        .warehouseId(order.getWarehouseId())
                        .sellerId(order.getSellerId())
                        .build()
        );

        log.info(">>> Order confirmed: {}", event.getOrderId());
    }

    // Order Failed ( Insufficient Order )
    @Transactional
    @Retryable( // Only retry for temporary database locks/concurrency issues
            retryFor = { ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 150,multiplier = 2))
    public void failOrder(StockFailedEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        order.setStatus(OrderStatus.FAILED);
        order.setRemarks("Failed: " + event.getReason());
        orderRepository.save(order);

        log.info(">>> Order failed: {} reason: {}", event.getOrderId(), event.getReason());
    }

    // ===== Deliver Order (called by RabbitMQ consumer) =====
    @Transactional
    @Retryable( // Only retry for temporary database locks/concurrency issues
            retryFor = { ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 150, multiplier = 2))
    public void deliverOrder(OrderDeliveredEvent event) {

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info(">>> Order delivered: {}", event.getOrderId());
    }

    // ===== Cancel Order =====
    @Transactional
    public OrderResponse cancelOrder(String orderId, String tenantId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!order.getTenantId().equals(tenantId)) {
            throw new UnauthorizedOrderAccessException("Not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot cancel order in " + order.getStatus() + " state");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info(">>> Order cancelled: {}", orderId);
        return mapToResponse(order);
    }

    // ===== Get Order =====
    public OrderResponse getOrder(String orderId, String tenantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!order.getTenantId().equals(tenantId)) {
            throw new UnauthorizedOrderAccessException("Not authorized to view this order");
        }

        return mapToResponse(order);
    }

    // ===== Get Orders by Seller =====
    public List<OrderResponse> getOrdersBySeller(
            String sellerId, String tenantId) {
        return orderRepository
                .findBySellerIdAndTenantId(sellerId, tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== Get Orders by Tenant =====
    public List<OrderResponse> getOrdersByTenant(String tenantId) {
        return orderRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== Get Orders by Status =====
    public List<OrderResponse> getOrdersByStatus(
            String tenantId, OrderStatus status) {
        return orderRepository
                .findByTenantIdAndStatus(tenantId, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== Map to Response =====
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null ?
                List.of() :
                order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .skuId(item.getSkuId())
                                .quantity(item.getQuantity())
                                .warehouseId(item.getWarehouseId())
                                .status(item.getStatus())
                                .build())
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .sellerId(order.getSellerId())
                .tenantId(order.getTenantId())
                .warehouseId(order.getWarehouseId())
                .status(order.getStatus())
                .remarks(order.getRemarks())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(order.getConfirmedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}