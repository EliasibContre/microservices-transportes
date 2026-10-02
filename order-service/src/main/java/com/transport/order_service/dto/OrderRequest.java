package com.transport.order_service.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank
        String origin,
        @NotBlank
        String destination
) {
}
