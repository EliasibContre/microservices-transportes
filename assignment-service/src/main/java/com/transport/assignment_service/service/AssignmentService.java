package com.transport.assignment_service.service;

import com.transport.assignment_service.client.DriverClient;
import com.transport.assignment_service.client.OrderClient;
import com.transport.assignment_service.dto.AssignmentRequest;
import com.transport.assignment_service.dto.AssignmentResponse;
import com.transport.assignment_service.entity.Assignment;
import com.transport.assignment_service.exception.ApplicationException;
import com.transport.assignment_service.exception.ApplicationException.Reason;
import com.transport.assignment_service.mapper.AssignmentMapper;
import com.transport.assignment_service.repository.AssignmentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final OrderClient orderClient;
    private final DriverClient driverClient;
    private final AssignmentMapper assignmentMapper;

    @Transactional
    public AssignmentResponse create(AssignmentRequest request) {
        OrderClient.OrderSnapshot order =
                findOrder(request.orderId());
        DriverClient.DriverSnapshot driver =
                findDriver(request.driverId());

        if (!"CREATED".equals(order.status())) {
            throw new ApplicationException(
                    Reason.BUSINESS_RULE,
                    "Solo se pueden asignar órdenes en estado CREATED");
        }

        if (!driver.active()) {
            throw new ApplicationException(
                    Reason.BUSINESS_RULE,
                    "El conductor debe estar activo");
        }

        if (assignmentRepository.existsByOrderId(request.orderId()))
        {
            throw new ApplicationException(
                    Reason.BUSINESS_RULE,
                    "La orden ya tiene un conductor asignado");
        }

        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setId(UUID.randomUUID());

        assignment.setAssignedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return assignmentMapper.toResponse(
                assignmentRepository.saveAndFlush(assignment));
    }

    private OrderClient.OrderSnapshot findOrder(UUID id) {
        try {
            return orderClient.findById(id);
        } catch (FeignException.NotFound ex) {
            throw new ApplicationException(
                    Reason.RESOURCE_NOT_FOUND,
                    "No existe la orden con ID " + id);
        }
    }

    private DriverClient.DriverSnapshot findDriver(UUID id) {
        try {
            return driverClient.findById(id);
        } catch (FeignException.NotFound ex) {
            throw new ApplicationException(
                    Reason.RESOURCE_NOT_FOUND,
                    "No existe el conductor con ID " + id);
        }
    }
}