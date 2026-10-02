package com.transport.driver_service.mapper;

import com.transport.driver_service.dto.DriverRequest;
import com.transport.driver_service.dto.DriverResponse;
import com.transport.driver_service.entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Driver toEntity (DriverRequest request);

    DriverResponse toResponse(Driver driver);
}

