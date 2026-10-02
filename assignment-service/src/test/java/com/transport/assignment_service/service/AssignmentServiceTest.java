package com.transport.assignment_service.service;

import com.transport.assignment_service.client.DriverClient;
import com.transport.assignment_service.client.OrderClient;
import com.transport.assignment_service.dto.AssignmentRequest;
import com.transport.assignment_service.dto.AssignmentResponse;
import com.transport.assignment_service.entity.Assignment;
import com.transport.assignment_service.exception.ApplicationException;
import com.transport.assignment_service.mapper.AssignmentMapper;
import com.transport.assignment_service.repository.AssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private DriverClient driverClient;

    @Mock
    private AssignmentMapper assignmentMapper;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    void createsAssignmentForCreatedOrderAndActiveDriver() {
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        AssignmentRequest request = new AssignmentRequest(orderId, driverId);
        when(orderClient.findById(orderId))
                .thenReturn(new OrderClient.OrderSnapshot(orderId, "CREATED"));
        when(driverClient.findById(driverId))
                .thenReturn(new DriverClient.DriverSnapshot(driverId, true));

        Assignment entity = new Assignment();
        entity.setOrderId(orderId);
        entity.setDriverId(driverId);
        when(assignmentMapper.toEntity(request)).thenReturn(entity);
        when(assignmentRepository.saveAndFlush(any(Assignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentMapper.toResponse(any(Assignment.class)))
                .thenAnswer(invocation -> {
                    Assignment saved = invocation.getArgument(0);
                    return new AssignmentResponse(saved.getId(), saved.getOrderId(),
                            saved.getDriverId(), saved.getAssignedAt());
                });

        AssignmentResponse response = assignmentService.create(request);

        assertNotNull(response.id());
        assertNotNull(response.assignedAt());
        assertEquals(orderId, response.orderId());
        assertEquals(driverId, response.driverId());
        ArgumentCaptor<Assignment> saved = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).saveAndFlush(saved.capture());
        assertEquals(orderId, saved.getValue().getOrderId());
        assertEquals(driverId, saved.getValue().getDriverId());
    }

    @Test
    void rejectsOrderOutsideCreatedStatus() {
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(orderClient.findById(orderId))
                .thenReturn(new OrderClient.OrderSnapshot(orderId, "IN_TRANSIT"));
        when(driverClient.findById(driverId))
                .thenReturn(new DriverClient.DriverSnapshot(driverId, true));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> assignmentService.create(new AssignmentRequest(orderId, driverId)));

        assertEquals(ApplicationException.Reason.BUSINESS_RULE, exception.reason());
        verifyNoInteractions(assignmentRepository, assignmentMapper);
    }

    @Test
    void rejectsInactiveDriver() {
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(orderClient.findById(orderId))
                .thenReturn(new OrderClient.OrderSnapshot(orderId, "CREATED"));
        when(driverClient.findById(driverId))
                .thenReturn(new DriverClient.DriverSnapshot(driverId, false));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> assignmentService.create(new AssignmentRequest(orderId, driverId)));

        assertEquals(ApplicationException.Reason.BUSINESS_RULE, exception.reason());
        verifyNoInteractions(assignmentRepository, assignmentMapper);
    }

    @Test
    void rejectsOrderAlreadyAssigned() {
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(orderClient.findById(orderId))
                .thenReturn(new OrderClient.OrderSnapshot(orderId, "CREATED"));
        when(driverClient.findById(driverId))
                .thenReturn(new DriverClient.DriverSnapshot(driverId, true));
        when(assignmentRepository.existsByOrderId(orderId)).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> assignmentService.create(new AssignmentRequest(orderId, driverId)));

        assertEquals(ApplicationException.Reason.BUSINESS_RULE, exception.reason());
        verify(assignmentRepository, never()).saveAndFlush(any(Assignment.class));
        verifyNoInteractions(assignmentMapper);
    }
}
