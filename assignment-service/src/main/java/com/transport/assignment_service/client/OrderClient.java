package com.transport.assignment_service.client;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    OrderSnapshot findById(@PathVariable("id") UUID id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OrderSnapshot(UUID id, String status) {
    }
}