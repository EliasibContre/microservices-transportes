package com.transport.order_service.service;

import com.transport.order_service.dto.OrderRequest;
import com.transport.order_service.dto.OrderResponse;
import com.transport.order_service.entity.Order;
import com.transport.order_service.enums.OrderStatus;
import com.transport.order_service.exception.InvalidOrderStatusTransitionException;
import com.transport.order_service.exception.OrderNotFoundException;
import com.transport.order_service.mapper.OrderMapper;
import com.transport.order_service.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private  final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return orderMapper.toResponse(order);
    }


    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(
            OrderStatus status,
            LocalDate date,
            String origin,
            String destination) {

        Specification<Order> filters = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (date != null) {
                OffsetDateTime start =
                        date.atStartOfDay().atOffset(ZoneOffset.UTC);
                OffsetDateTime end =
                        date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

                predicates.add(cb.greaterThanOrEqualTo(
                        root.<OffsetDateTime>get("createdAt"), start));
                predicates.add(cb.lessThan(
                        root.<OffsetDateTime>get("createdAt"), end));
            }

            if (origin != null && !origin.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("origin")),
                        origin.trim().toLowerCase(Locale.ROOT)));
            }

            if (destination != null && !destination.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("destination")),
                        destination.trim().toLowerCase(Locale.ROOT)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(filters).stream()
                .map(orderMapper::toResponse)
                .toList();
    }







    @Transactional
    public OrderResponse create (OrderRequest request){
        Order order = orderMapper.toEntity(request);

        order.setId(UUID.randomUUID());

        order.setStatus(OrderStatus.CREATED);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        order.setCreatedAt(now);

        order.setUpdatedAt(now);

        Order saved = orderRepository.save(order);

        log.atInfo()
                .addKeyValue("event", "order_created")
                .addKeyValue("orderId", saved.getId().toString())
                .addKeyValue("status", saved.getStatus().name())
                .log("Orden creada");

        return orderMapper.toResponse(saved);
    }


    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus nextStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus currentStatus = order.getStatus();

        if (!canTransition(currentStatus, nextStatus)) {
            log.atWarn()
                    .addKeyValue("event", "order_status_change_rejected")
                    .addKeyValue("orderId", id.toString())
                    .addKeyValue("currentStatus", currentStatus.name())
                    .addKeyValue("requestedStatus", nextStatus.name())
                    .log("Transición de estado rechazada");

            throw new InvalidOrderStatusTransitionException(currentStatus,
                    nextStatus);
        }

        order.setStatus(nextStatus);
        order.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Order saved = orderRepository.save(order);

        log.atInfo()
                .addKeyValue("event", "order_status_changed")
                .addKeyValue("orderId", id.toString())
                .addKeyValue("previousStatus", currentStatus.name())
                .addKeyValue("newStatus", nextStatus.name())
                .log("Estado de orden actualizado");

        return orderMapper.toResponse(saved);
    }



    private boolean canTransition(OrderStatus current, OrderStatus next){
        return switch (current) {
            case CREATED ->
                    next == OrderStatus.IN_TRANSIT || next ==
                            OrderStatus.CANCELLED;
            case IN_TRANSIT ->
                    next == OrderStatus.DELIVERED || next ==
                            OrderStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
