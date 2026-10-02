package com.transport.order_service.dto;

import com.transport.order_service.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull
        OrderStatus status
) {
}
