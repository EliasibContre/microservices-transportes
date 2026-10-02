package com.transport.driver_service.service;

import com.transport.driver_service.dto.DriverRequest;
import com.transport.driver_service.dto.DriverResponse;
import com.transport.driver_service.entity.Driver;
import com.transport.driver_service.mapper.DriverMapper;
import com.transport.driver_service.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverMapper driverMapper;

    @InjectMocks
    private DriverService driverService;

    @Test
    void createsActiveDriver() {
        DriverRequest request = new DriverRequest("Ana López", "LIC-001");
        Driver entity = new Driver();
        entity.setName(request.name());
        entity.setLicenseNumber(request.licenseNumber());
        when(driverMapper.toEntity(request)).thenReturn(entity);
        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(driverMapper.toResponse(any(Driver.class)))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        DriverResponse created = driverService.create(request);

        assertNotNull(created.id());
        assertEquals("Ana López", created.name());
        assertEquals("LIC-001", created.licenseNumber());
        assertTrue(created.active());
        verify(driverRepository).save(entity);
    }

    @Test
    void listsActiveDrivers() {
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setName("Ana López");
        driver.setLicenseNumber("LIC-001");
        driver.setActive(true);
        when(driverRepository.findByActiveTrue()).thenReturn(List.of(driver));
        when(driverMapper.toResponse(driver)).thenReturn(response(driver));

        List<DriverResponse> drivers = driverService.findActive();

        assertEquals(1, drivers.size());
        assertEquals(driver.getId(), drivers.get(0).id());
        assertTrue(drivers.get(0).active());
        verify(driverRepository).findByActiveTrue();
    }

    @Test
    void reportsMissingDriver() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> driverService.findById(id));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private static DriverResponse response(Driver driver) {
        return new DriverResponse(driver.getId(), driver.getName(),
                driver.getLicenseNumber(), driver.isActive());
    }
}
