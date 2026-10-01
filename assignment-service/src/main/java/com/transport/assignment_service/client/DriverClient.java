package com.transport.assignment_service.client;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "driver-service")
public interface DriverClient {

    @GetMapping("/api/drivers/{id}")
    DriverSnapshot findById(@PathVariable("id") UUID id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DriverSnapshot(UUID id, boolean active) {
    }
}