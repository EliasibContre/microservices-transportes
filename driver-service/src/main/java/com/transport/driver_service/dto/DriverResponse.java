package com.transport.driver_service.dto;

import java.util.UUID;

public record DriverResponse(

        UUID id,
        String name,
        String licenseNumber,
        boolean active

) {
}
