package com.fleetmaster.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmaster.dtos.AddProgressDto;
import com.fleetmaster.dtos.CreateOrderDto;
import com.fleetmaster.exceptions.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Query query;

    @Mock
    private NativeQuery nativeQuery;

    private OrderService orderService;

    private CreateOrderDto createOrderDto;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(objectMapper);
        // Manually inject the mocked EntityManager using reflection
        try {
            java.lang.reflect.Field field = OrderService.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(orderService, entityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        createOrderDto = new CreateOrderDto();
        createOrderDto.setVehicleId(1L);
        createOrderDto.setDriverId(1L);
        createOrderDto.setPickUpLat(40.7128);
        createOrderDto.setPickUpLon(-74.0060);
        createOrderDto.setDeliveryLat(34.0522);
        createOrderDto.setDeliveryLon(-118.2437);
        createOrderDto.setLoadType("NORMAL");
        createOrderDto.setDepartureTime(LocalDateTime.now());
        createOrderDto.setArrivalTime(LocalDateTime.now().plusHours(5));
    }

    @Test
    void testCreateOrder_Success() {
        // Given
        Long companyId = 1L;
        Long expectedOrderId = 100L;

        // Mock vehicle ownership check
        Query vehicleCheckQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("company_vehicles")))
                .thenReturn(vehicleCheckQuery);
        when(vehicleCheckQuery.setParameter(anyString(), any())).thenReturn(vehicleCheckQuery);
        when(vehicleCheckQuery.getResultList()).thenReturn(Collections.singletonList(1));

        // Mock driver ownership check
        Query driverCheckQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("company_account")))
                .thenReturn(driverCheckQuery);
        when(driverCheckQuery.setParameter(anyString(), any())).thenReturn(driverCheckQuery);
        when(driverCheckQuery.getResultList()).thenReturn(Collections.singletonList(1));

        // Mock stored procedure call
        Query spQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("sp_create_order")))
                .thenReturn(spQuery);
        when(spQuery.setParameter(anyString(), any())).thenReturn(spQuery);
        when(spQuery.getSingleResult()).thenReturn(expectedOrderId);

        // When
        Long orderId = orderService.createOrder(companyId, createOrderDto);

        // Then
        assertNotNull(orderId);
        assertEquals(expectedOrderId, orderId);
        verify(spQuery).getSingleResult();
    }

    @Test
    void testCreateOrder_VehicleNotOwned() {
        // Given
        Long companyId = 1L;

        Query vehicleCheckQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("company_vehicles")))
                .thenReturn(vehicleCheckQuery);
        when(vehicleCheckQuery.setParameter(anyString(), any())).thenReturn(vehicleCheckQuery);
        when(vehicleCheckQuery.getResultList()).thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(companyId, createOrderDto);
        });

        assertEquals("Vehicle does not belong to your company", exception.getMessage());
    }

    @Test
    void testCreateOrder_DriverNotOwned() {
        // Given
        Long companyId = 1L;

        // Mock vehicle ownership check (pass)
        Query vehicleCheckQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("company_vehicles")))
                .thenReturn(vehicleCheckQuery);
        when(vehicleCheckQuery.setParameter(anyString(), any())).thenReturn(vehicleCheckQuery);
        when(vehicleCheckQuery.getResultList()).thenReturn(Collections.singletonList(1));

        // Mock driver ownership check (fail)
        Query driverCheckQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("company_account")))
                .thenReturn(driverCheckQuery);
        when(driverCheckQuery.setParameter(anyString(), any())).thenReturn(driverCheckQuery);
        when(driverCheckQuery.getResultList()).thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(companyId, createOrderDto);
        });

        assertEquals("Driver does not belong to your company", exception.getMessage());
    }

    @Test
    void testGetAllOrders_Success() {
        // Given
        Long companyId = 1L;
        List<Map<String, Object>> expectedOrders = new ArrayList<>();
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 100L);
        order1.put("status", "IN_PROGRESS");
        expectedOrders.add(order1);

        when(entityManager.createNativeQuery(contains("SELECT o.* FROM orders")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(expectedOrders);

        // When
        List<Map<String, Object>> orders = orderService.getAllOrders(companyId);

        // Then
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(100L, orders.get(0).get("id"));
    }

    @Test
    void testGetOrderById_Success() {
        // Given
        Long companyId = 1L;
        Long orderId = 100L;
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        order.put("status", "IN_PROGRESS");
        results.add(order);

        when(entityManager.createNativeQuery(contains("SELECT o.* FROM orders")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(results);

        // When
        Map<String, Object> result = orderService.getOrderById(companyId, orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.get("id"));
    }

    @Test
    void testGetOrderById_NotFound() {
        // Given
        Long companyId = 1L;
        Long orderId = 999L;

        when(entityManager.createNativeQuery(contains("SELECT o.* FROM orders")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.getOrderById(companyId, orderId);
        });

        assertEquals("Order not found or does not belong to your company", exception.getMessage());
    }

    @Test
    void testUpdateOrderStatus_Success() {
        // Given
        Long companyId = 1L;
        Long orderId = 100L;
        String newStatus = "COMPLETED";

        // Mock getOrderById (ownership check)
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        results.add(order);

        Query selectQuery = mock(Query.class);
        Query updateQuery = mock(Query.class);
        NativeQuery selectNativeQuery = mock(NativeQuery.class);

        when(entityManager.createNativeQuery(contains("SELECT o.* FROM orders")))
                .thenReturn(selectQuery);
        when(selectQuery.setParameter(anyString(), any())).thenReturn(selectQuery);
        when(selectQuery.unwrap(NativeQuery.class)).thenReturn(selectNativeQuery);
        when(selectNativeQuery.setResultTransformer(any())).thenReturn(selectNativeQuery);
        when(selectNativeQuery.getResultList()).thenReturn(results);

        when(entityManager.createNativeQuery(contains("UPDATE orders SET status")))
                .thenReturn(updateQuery);
        when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        // When
        assertDoesNotThrow(() -> orderService.updateOrderStatus(companyId, orderId, newStatus));

        // Then
        verify(updateQuery).executeUpdate();
    }

    @Test
    void testDeleteOrder_Success() {
        // Given
        Long companyId = 1L;
        Long orderId = 100L;

        // Mock getOrderById (ownership check)
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        results.add(order);

        Query selectQuery = mock(Query.class);
        Query deleteProgressQuery = mock(Query.class);
        Query deleteOrderQuery = mock(Query.class);
        NativeQuery selectNativeQuery = mock(NativeQuery.class);

        when(entityManager.createNativeQuery(contains("SELECT o.* FROM orders")))
                .thenReturn(selectQuery);
        when(selectQuery.setParameter(anyString(), any())).thenReturn(selectQuery);
        when(selectQuery.unwrap(NativeQuery.class)).thenReturn(selectNativeQuery);
        when(selectNativeQuery.setResultTransformer(any())).thenReturn(selectNativeQuery);
        when(selectNativeQuery.getResultList()).thenReturn(results);

        when(entityManager.createNativeQuery(contains("DELETE FROM progress")))
                .thenReturn(deleteProgressQuery);
        when(deleteProgressQuery.setParameter(anyString(), any())).thenReturn(deleteProgressQuery);
        when(deleteProgressQuery.executeUpdate()).thenReturn(1);

        when(entityManager.createNativeQuery(contains("DELETE FROM orders")))
                .thenReturn(deleteOrderQuery);
        when(deleteOrderQuery.setParameter(anyString(), any())).thenReturn(deleteOrderQuery);
        when(deleteOrderQuery.executeUpdate()).thenReturn(1);

        // When
        assertDoesNotThrow(() -> orderService.deleteOrder(companyId, orderId));

        // Then
        verify(deleteProgressQuery).executeUpdate();
        verify(deleteOrderQuery).executeUpdate();
    }

    @Test
    void testAddProgress_Success() throws Exception {
        // Given
        Long companyId = 1L;
        Long expectedProgressId = 50L;

        AddProgressDto progressDto = new AddProgressDto();
        progressDto.setOrderId(100L);
        progressDto.setLat(40.7128);
        progressDto.setLon(-74.0060);
        progressDto.setType("CHECKPOINT");
        Map<String, Object> description = new HashMap<>();
        description.put("note", "Arrived at checkpoint");
        progressDto.setDescription(description);

        // Mock order ownership check
        Query orderCheckQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("orders o JOIN company_vehicles")))
                .thenReturn(orderCheckQuery);
        when(orderCheckQuery.setParameter(anyString(), any())).thenReturn(orderCheckQuery);
        when(orderCheckQuery.getResultList()).thenReturn(Collections.singletonList(1));

        // Mock objectMapper
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"note\":\"Arrived at checkpoint\"}");

        // Mock stored procedure call
        Query spQuery = mock(Query.class);
        when(entityManager.createNativeQuery(contains("sp_add_progress")))
                .thenReturn(spQuery);
        when(spQuery.setParameter(anyString(), any())).thenReturn(spQuery);
        when(spQuery.getSingleResult()).thenReturn(expectedProgressId);

        // When
        Long progressId = orderService.addProgress(companyId, progressDto);

        // Then
        assertNotNull(progressId);
        assertEquals(expectedProgressId, progressId);
        verify(spQuery).getSingleResult();
    }

    @Test
    void testGetOrderProgress_Success() {
        // Given
        Long companyId = 1L;
        Long orderId = 100L;

        // Mock getOrderById (ownership check)
        List<Map<String, Object>> orderResults = new ArrayList<>();
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        orderResults.add(order);

        Query selectOrderQuery = mock(Query.class);
        NativeQuery selectOrderNativeQuery = mock(NativeQuery.class);

        when(entityManager.createNativeQuery(contains("SELECT o.* FROM orders")))
                .thenReturn(selectOrderQuery);
        when(selectOrderQuery.setParameter(anyString(), any())).thenReturn(selectOrderQuery);
        when(selectOrderQuery.unwrap(NativeQuery.class)).thenReturn(selectOrderNativeQuery);
        when(selectOrderNativeQuery.setResultTransformer(any())).thenReturn(selectOrderNativeQuery);
        when(selectOrderNativeQuery.getResultList()).thenReturn(orderResults);

        // Mock progress query
        List<Map<String, Object>> progressResults = new ArrayList<>();
        Map<String, Object> progress1 = new HashMap<>();
        progress1.put("id", 1L);
        progress1.put("type", "CHECKPOINT");
        progressResults.add(progress1);

        Query progressQuery = mock(Query.class);
        NativeQuery progressNativeQuery = mock(NativeQuery.class);

        when(entityManager.createNativeQuery(contains("SELECT * FROM progress")))
                .thenReturn(progressQuery);
        when(progressQuery.setParameter(anyString(), any())).thenReturn(progressQuery);
        when(progressQuery.unwrap(NativeQuery.class)).thenReturn(progressNativeQuery);
        when(progressNativeQuery.setResultTransformer(any())).thenReturn(progressNativeQuery);
        when(progressNativeQuery.getResultList()).thenReturn(progressResults);

        // When
        List<Map<String, Object>> progress = orderService.getOrderProgress(companyId, orderId);

        // Then
        assertNotNull(progress);
        assertEquals(1, progress.size());
        assertEquals("CHECKPOINT", progress.get(0).get("type"));
    }
}
