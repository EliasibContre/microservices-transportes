package com.transport.order_service.dto;

import com.transport.order_service.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        String origin,
        String destination,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
