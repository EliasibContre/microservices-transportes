package com.transport.driver_service.service;

import com.transport.driver_service.dto.DriverRequest;
import com.transport.driver_service.dto.DriverResponse;
import com.transport.driver_service.entity.Driver;
import com.transport.driver_service.mapper.DriverMapper;
import com.transport.driver_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    @Transactional
    public DriverResponse create(DriverRequest request) {

        Driver driver = driverMapper.toEntity(request);

        driver.setId(UUID.randomUUID());

        driver.setActive(true);

        Driver saved = driverRepository.save(driver);

        log.atInfo()
                .addKeyValue("event", "driver_created")
                .addKeyValue("driverId", saved.getId().toString())
                .addKeyValue("active", saved.isActive())
                .log("Conductor creado");

        return driverMapper.toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<DriverResponse> findActive() {

        List<DriverResponse> drivers =
                driverRepository.findByActiveTrue().stream()
                        .map(driverMapper::toResponse)
                        .toList();

        log.atInfo()
                .addKeyValue("event", "active_drivers_listed")
                .addKeyValue("count", drivers.size())
                .log("Conductores activos consultados");

        return drivers;
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(UUID id) {
        return driverRepository.findById(id)
                .map(driverMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el conductor con ID " + id));
    }
}
