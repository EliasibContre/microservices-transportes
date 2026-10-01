package com.transport.driver_service.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail responseStatus(
            ResponseStatusException ex, HttpServletRequest request)
    {
        String title = ex.getStatusCode().value() == 404
                ? "DRIVER_NOT_FOUND" : "REQUEST_ERROR";
        String detail = ex.getReason() == null
                ? "No se pudo procesar la solicitud" :
                ex.getReason();

        return problem(ex.getStatusCode(), title, detail, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String detail =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": "
                                + error.getDefaultMessage())
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
            HttpStatusCode status, String title, String detail,
            HttpServletRequest request) {
        ProblemDetail result =
                ProblemDetail.forStatusAndDetail(status, detail);
        result.setTitle(title);
        result.setInstance(URI.create(request.getRequestURI()));
        return result;
    }
}