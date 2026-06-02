package com.fulfillx.inbound;

import com.fulfillx.inbound.dto.*;
import com.fulfillx.inbound.entity.GRN;
import com.fulfillx.inbound.entity.GRNItem;
import com.fulfillx.inbound.enums.GRNItemStatus;
import com.fulfillx.inbound.enums.GRNStatus;
import com.fulfillx.inbound.event.InboundEventPublisher;
import com.fulfillx.inbound.exception.GRNNotFoundException;
import com.fulfillx.inbound.exception.InvalidGRNStateException;
import com.fulfillx.inbound.repository.GRNItemRepository;
import com.fulfillx.inbound.repository.GRNRepository;
import com.fulfillx.inbound.service.GRNService;
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
public class GRNServiceTest {

    @Mock
    private GRNRepository grnRepository;

    @Mock
    private GRNItemRepository grnItemRepository;

    @Mock
    private InboundEventPublisher eventPublisher;

    @InjectMocks
    private GRNService grnService;

    private GRN mockGRN;
    private GRNItem mockGRNItem;
    private CreateGRNRequest createRequest;

    @BeforeEach
    void setUp() {
        mockGRNItem = GRNItem.builder()
                .id("item-001")
                .skuId("sku-001")
                .quantity(100)
                .receivedQuantity(0)
                .status(GRNItemStatus.PENDING)
                .build();

        mockGRN = GRN.builder()
                .id("grn-001")
                .vendorId("vendor-001")
                .warehouseId("warehouse-001")
                .tenantId("tenant-001")
                .status(GRNStatus.DRAFT)
                .items(List.of(mockGRNItem))
                .build();

        mockGRNItem.setGrn(mockGRN);

        GRNItemRequest itemRequest = new GRNItemRequest();
        itemRequest.setSkuId("sku-001");
        itemRequest.setQuantity(100);

        createRequest = new CreateGRNRequest();
        createRequest.setVendorId("vendor-001");
        createRequest.setWarehouseId("warehouse-001");
        createRequest.setItems(List.of(itemRequest));
    }

    // ✅ Test 1 — Create GRN Successfully
    @Test
    void createGRN_ShouldReturnGRNResponse_WhenValidRequest() {
        when(grnRepository.save(any())).thenReturn(mockGRN);
        when(grnItemRepository.saveAll(any())).thenReturn(List.of(mockGRNItem));

        GRNResponse response = grnService.createGRN(createRequest, "tenant-001");

        assertNotNull(response);
        assertEquals("grn-001", response.getId());
        assertEquals("vendor-001", response.getVendorId());
        verify(grnRepository, times(1)).save(any());
    }

    // ✅ Test 2 — Confirm GRN Successfully
    @Test
    void confirmGRN_ShouldConfirmAndPublishEvent_WhenDraftGRN() {
        GRNItem acceptedItem = GRNItem.builder()
                .id("item-001")
                .skuId("sku-001")
                .quantity(100)
                .receivedQuantity(80)
                .status(GRNItemStatus.ACCEPTED)
                .grn(mockGRN)
                .build();

        when(grnRepository.findById("grn-001")).thenReturn(Optional.of(mockGRN));
        when(grnRepository.save(any())).thenReturn(mockGRN);
        when(grnItemRepository.findByGrnIdAndStatus("grn-001", GRNItemStatus.ACCEPTED))
                .thenReturn(List.of(acceptedItem));
        doNothing().when(eventPublisher).publishStockReceived(any());

        GRNResponse response = grnService.confirmGRN("grn-001");

        assertNotNull(response);
        verify(eventPublisher, times(1)).publishStockReceived(any());
    }

    // ✅ Test 3 — Confirm GRN Fails if Already Confirmed
    @Test
    void confirmGRN_ShouldThrowException_WhenGRNAlreadyConfirmed() {
        mockGRN.setStatus(GRNStatus.CONFIRMED);
        when(grnRepository.findById("grn-001")).thenReturn(Optional.of(mockGRN));

        assertThrows(InvalidGRNStateException.class,
                () -> grnService.confirmGRN("grn-001"));

        verify(eventPublisher, never()).publishStockReceived(any());
    }

    // ✅ Test 4 — Confirm GRN Fails if Not Found
    @Test
    void confirmGRN_ShouldThrowException_WhenGRNNotFound() {
        when(grnRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(GRNNotFoundException.class,
                () -> grnService.confirmGRN("invalid-id"));
    }

    // ✅ Test 5 — Scan Item Successfully
    @Test
    void scanItem_ShouldUpdateItem_WhenValidRequest() {
        when(grnItemRepository.findByGrnIdAndSkuId("grn-001", "sku-001"))
                .thenReturn(Optional.of(mockGRNItem));
        when(grnItemRepository.save(any())).thenReturn(mockGRNItem);

        ScanItemRequest request = new ScanItemRequest();
        request.setGrnId("grn-001");
        request.setSkuId("sku-001");
        request.setReceivedQuantity(80);
        request.setStatus(GRNItemStatus.ACCEPTED);

        GRNItemResponse response = grnService.scanItem(request);

        assertNotNull(response);
        verify(grnItemRepository, times(1)).save(any());
    }

    // ✅ Test 6 — Scan Item Fails if GRN Already Confirmed
    @Test
    void scanItem_ShouldThrowException_WhenGRNAlreadyConfirmed() {
        mockGRN.setStatus(GRNStatus.CONFIRMED);
        when(grnItemRepository.findByGrnIdAndSkuId("grn-001", "sku-001"))
                .thenReturn(Optional.of(mockGRNItem));

        ScanItemRequest request = new ScanItemRequest();
        request.setGrnId("grn-001");
        request.setSkuId("sku-001");
        request.setReceivedQuantity(80);
        request.setStatus(GRNItemStatus.ACCEPTED);

        assertThrows(InvalidGRNStateException.class,
                () -> grnService.scanItem(request));
    }

    // ✅ Test 7 — No Event Published if No Accepted Items
    @Test
    void confirmGRN_ShouldNotPublishEvent_WhenNoAcceptedItems() {
        when(grnRepository.findById("grn-001")).thenReturn(Optional.of(mockGRN));
        when(grnRepository.save(any())).thenReturn(mockGRN);
        when(grnItemRepository.findByGrnIdAndStatus("grn-001", GRNItemStatus.ACCEPTED))
                .thenReturn(List.of());

        grnService.confirmGRN("grn-001");

        verify(eventPublisher, never()).publishStockReceived(any());
    }

    // ✅ Test 8 — Get GRN by ID
    @Test
    void getGRN_ShouldReturnGRNResponse_WhenGRNExists() {
        when(grnRepository.findById("grn-001")).thenReturn(Optional.of(mockGRN));

        GRNResponse response = grnService.getGRN("grn-001");

        assertNotNull(response);
        assertEquals("grn-001", response.getId());
    }

    // ✅ Test 9 — Get GRN Fails if Not Found
    @Test
    void getGRN_ShouldThrowException_WhenGRNNotFound() {
        when(grnRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(GRNNotFoundException.class,
                () -> grnService.getGRN("invalid-id"));
    }
}