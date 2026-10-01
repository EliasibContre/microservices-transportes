package com.transport.order_service.mapper;


import com.transport.order_service.dto.OrderRequest;
import com.transport.order_service.dto.OrderResponse;
import com.transport.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity (OrderRequest request);

    OrderResponse toResponse(Order entity);


}
