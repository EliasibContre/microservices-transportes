package com.transport.driver_service.controller;


import com.transport.driver_service.dto.DriverRequest;
import com.transport.driver_service.dto.DriverResponse;
import com.transport.driver_service.service.DriverService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;


    @PostMapping
    public ResponseEntity<DriverResponse> create(
            @Valid
            @RequestBody
            DriverRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverService.create(request));
    }

    @GetMapping("/active")
    public List<DriverResponse> findActive(){
        return driverService.findActive();
    }


    @GetMapping("/{id}")
    public DriverResponse findById(@PathVariable UUID id) {
        return driverService.findById(id);
    }

}
