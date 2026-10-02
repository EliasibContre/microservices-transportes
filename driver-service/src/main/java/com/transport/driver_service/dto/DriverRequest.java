package com.transport.driver_service.dto;

import jakarta.validation.constraints.NotBlank;

public record DriverRequest(
        @NotBlank
        String name,
        @NotBlank
        String licenseNumber
) {
}
