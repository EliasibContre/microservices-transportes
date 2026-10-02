package com.transport.order_service.service;

import com.transport.order_service.dto.OrderRequest;
import com.transport.order_service.dto.OrderResponse;
import com.transport.order_service.entity.Order;
import com.transport.order_service.enums.OrderStatus;
import com.transport.order_service.exception.InvalidOrderStatusTransitionException;
import com.transport.order_service.exception.OrderNotFoundException;
import com.transport.order_service.mapper.OrderMapper;
import com.transport.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createsOrderInCreatedStatus() {
        OrderRequest request = new OrderRequest("México", "Puebla");
        Order entity = new Order();
        entity.setOrigin(request.origin());
        entity.setDestination(request.destination());
        when(orderMapper.toEntity(request)).thenReturn(entity);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class)))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        OrderResponse created = orderService.create(request);

        assertNotNull(created.id());
        assertEquals(OrderStatus.CREATED, created.status());
        assertEquals("México", created.origin());
        assertEquals("Puebla", created.destination());
        assertNotNull(created.createdAt());
        assertEquals(created.createdAt(), created.updatedAt());
        verify(orderRepository).save(entity);
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, IN_TRANSIT",
            "CREATED, CANCELLED",
            "IN_TRANSIT, DELIVERED",
            "IN_TRANSIT, CANCELLED"
    })
    void acceptsValidStatusTransitions(OrderStatus current, OrderStatus next) {
        UUID id = UUID.randomUUID();
        Order order = existingOrder(id, current);
        OffsetDateTime previousUpdate = order.getUpdatedAt();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenAnswer(invocation -> response(order));

        OrderResponse updated = orderService.updateStatus(id, next);

        assertEquals(next, updated.status());
        assertEquals(id, updated.id());
        assertTrue(updated.updatedAt().isAfter(previousUpdate));
        verify(orderRepository).save(order);
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, CREATED",
            "CREATED, DELIVERED",
            "IN_TRANSIT, CREATED",
            "DELIVERED, IN_TRANSIT",
            "CANCELLED, IN_TRANSIT"
    })
    void rejectsInvalidStatusTransitions(OrderStatus current, OrderStatus next) {
        UUID id = UUID.randomUUID();
        Order order = existingOrder(id, current);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateStatus(id, next));

        assertEquals(current, order.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void reportsMissingOrder() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findById(id));
    }

    private static Order existingOrder(UUID id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setOrigin("México");
        order.setDestination("Puebla");
        order.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        order.setUpdatedAt(order.getCreatedAt());
        return order;
    }

    private static OrderResponse response(Order order) {
        return new OrderResponse(order.getId(), order.getStatus(), order.getOrigin(),
                order.getDestination(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
