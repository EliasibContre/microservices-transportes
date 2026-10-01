package com.transport.order_service.exception;

import com.transport.order_service.enums.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException{
    public InvalidOrderStatusTransitionException (OrderStatus current, OrderStatus next){
        super("No se permite cambiar la orden de " + current + " a " + next);
    }
}
