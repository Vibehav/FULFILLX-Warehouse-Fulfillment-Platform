package com.fulfillx.order;

import com.fulfillx.order.dto.*;
import com.fulfillx.order.entity.Order;
import com.fulfillx.order.entity.OrderItem;
import com.fulfillx.order.enums.OrderItemStatus;
import com.fulfillx.order.enums.OrderStatus;
import com.fulfillx.order.event.*;
import com.fulfillx.order.exception.*;
import com.fulfillx.order.repository.OrderItemRepository;
import com.fulfillx.order.repository.OrderRepository;
import com.fulfillx.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private Publisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order mockOrder;
    private OrderItem mockOrderItem;
    private CreateOrderRequest createRequest;
    private StockReservedEvent stockReservedEvent;
    private OrderDeliveredEvent orderDeliveredEvent;

    @BeforeEach
    void setUp() {
        mockOrderItem = OrderItem.builder()
                .id("item-001")
                .skuId("sku-001")
                .quantity(10)
                .warehouseId("warehouse-001")
                .status(OrderItemStatus.PENDING)
                .build();

        mockOrder = Order.builder()
                .id("order-001")
                .sellerId("seller-001")
                .tenantId("tenant-001")
                .warehouseId("warehouse-001")
                .status(OrderStatus.PENDING)
                .items(List.of(mockOrderItem))
                .build();

        mockOrderItem.setOrder(mockOrder);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setSkuId("sku-001");
        itemRequest.setQuantity(10);
        itemRequest.setWarehouseId("warehouse-001");

        createRequest = new CreateOrderRequest();
        createRequest.setWarehouseId("warehouse-001");
        createRequest.setItems(List.of(itemRequest));

        stockReservedEvent = StockReservedEvent.builder()
                .orderId("order-001")
                .tenantId("tenant-001")
                .warehouseId("warehouse-001")
                .items(List.of(
                        StockReservedEvent.ReservedItem.builder()
                                .skuId("sku-001")
                                .quantity(10)
                                .build()
                ))
                .build();

        orderDeliveredEvent = OrderDeliveredEvent.builder()
                .orderId("order-001")
                .tenantId("tenant-001")
                .build();
    }

    // ✅ Test 1 — Create Order Successfully
    @Test
    void createOrder_ShouldReturnOrderResponse_WhenValidRequest() {
        when(orderRepository.save(any())).thenReturn(mockOrder);
        when(orderItemRepository.saveAll(any())).thenReturn(List.of(mockOrderItem));
        doNothing().when(eventPublisher).publishOrderCreated(any());

        OrderResponse response = orderService.createOrder(
                createRequest, "seller-001", "tenant-001");

        assertNotNull(response);
        assertEquals("order-001", response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(eventPublisher, times(1)).publishOrderCreated(any());
    }

    // ✅ Test 2 — Confirm Order Successfully
    @Test
    void confirmOrder_ShouldConfirmOrder_WhenStockReserved() {
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));
        when(orderItemRepository.findByOrderId("order-001"))
                .thenReturn(List.of(mockOrderItem));
        when(orderItemRepository.saveAll(any()))
                .thenReturn(List.of(mockOrderItem));
        when(orderRepository.save(any())).thenReturn(mockOrder);
        doNothing().when(eventPublisher).publishOrderConfirmed(any());

        orderService.confirmOrder(stockReservedEvent);

        assertEquals(OrderStatus.CONFIRMED, mockOrder.getStatus());
        assertEquals(OrderItemStatus.RESERVED, mockOrderItem.getStatus());
        verify(eventPublisher, times(1)).publishOrderConfirmed(any());
    }

    // ✅ Test 3 — Confirm Order Skips if Not Pending
    @Test
    void confirmOrder_ShouldSkip_WhenOrderNotPending() {
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));

        orderService.confirmOrder(stockReservedEvent);

        verify(eventPublisher, never()).publishOrderConfirmed(any());
    }

    // ✅ Test 4 — Deliver Order Successfully
    @Test
    void deliverOrder_ShouldMarkDelivered_WhenValidOrder() {
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any())).thenReturn(mockOrder);

        orderService.deliverOrder(orderDeliveredEvent);

        assertEquals(OrderStatus.DELIVERED, mockOrder.getStatus());
        assertNotNull(mockOrder.getDeliveredAt());
    }

    // ✅ Test 5 — Cancel Order Successfully
    @Test
    void cancelOrder_ShouldCancelOrder_WhenPending() {
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any())).thenReturn(mockOrder);

        OrderResponse response = orderService.cancelOrder(
                "order-001", "tenant-001");

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
    }

    // ✅ Test 6 — Cancel Order Fails if Shipped
    @Test
    void cancelOrder_ShouldThrowException_WhenOrderShipped() {
        mockOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.cancelOrder("order-001", "tenant-001"));
    }

    // ✅ Test 7 — Cancel Order Fails if Wrong Tenant
    @Test
    void cancelOrder_ShouldThrowException_WhenWrongTenant() {
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));

        assertThrows(UnauthorizedOrderAccessException.class,
                () -> orderService.cancelOrder("order-001", "wrong-tenant"));
    }

    // ✅ Test 8 — Get Order Successfully
    @Test
    void getOrder_ShouldReturnOrder_WhenExists() {
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderService.getOrder(
                "order-001", "tenant-001");

        assertNotNull(response);
        assertEquals("order-001", response.getId());
    }

    // ✅ Test 9 — Get Order Fails if Not Found
    @Test
    void getOrder_ShouldThrowException_WhenNotFound() {
        when(orderRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrder("invalid", "tenant-001"));
    }

    // ✅ Test 10 — Get Order Fails if Wrong Tenant
    @Test
    void getOrder_ShouldThrowException_WhenWrongTenant() {
        when(orderRepository.findById("order-001"))
                .thenReturn(Optional.of(mockOrder));

        assertThrows(UnauthorizedOrderAccessException.class,
                () -> orderService.getOrder("order-001", "wrong-tenant"));
    }
}