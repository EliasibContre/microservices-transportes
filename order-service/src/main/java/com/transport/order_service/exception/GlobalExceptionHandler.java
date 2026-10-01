package com.transport.order_service.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail orderNotFound(
            OrderNotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND",
                ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ProblemDetail invalidTransition(
            InvalidOrderStatusTransitionException ex,
            HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT,
                "INVALID_STATUS_TRANSITION",
                ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String detail =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " +
                                error.getDefaultMessage())
                        .collect(Collectors.joining("; "));

        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                detail, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ProblemDetail invalidInput(
            Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_INPUT",
                "La petición contiene un valor o JSON inválido",
                request);
    }

    private ProblemDetail problem(
            HttpStatus status, String code, String detail,
            HttpServletRequest request) {
        ProblemDetail result =
                ProblemDetail.forStatusAndDetail(status, detail);
        result.setTitle(code);
        result.setInstance(URI.create(request.getRequestURI()));
        return result;
    }
}
