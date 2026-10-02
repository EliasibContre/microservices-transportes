package com.transport.assignment_service.exception;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.UncheckedIOException;
import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail applicationException(
            ApplicationException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex.reason()) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BUSINESS_RULE -> HttpStatus.CONFLICT;
            case INVALID_FILE -> HttpStatus.BAD_REQUEST;
        };

        log.atWarn()
                .addKeyValue("event", "assignment_request_rejected")
                .addKeyValue("reason", ex.reason().name())
                .addKeyValue("status", status.value())
                .addKeyValue("path", request.getRequestURI())
                .log("Solicitud de asignación rechazada");

        return problem(status, ex.reason().name(),
                ex.getMessage(), request);
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
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class
    })
    public ProblemDetail invalidInput(
            Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_INPUT",
                "La petición contiene datos inválidos o falta el archivo",
                request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail fileTooLarge(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE,
                "FILE_TOO_LARGE",
                "El archivo supera el tamaño permitido", request);
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail dependencyFailure(
            FeignException ex, HttpServletRequest request) {
        boolean unavailable = ex.status() < 0
                || ex.status() == 503
                || ex.status() == 504;

        HttpStatus status = unavailable
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.BAD_GATEWAY;

        log.atWarn()
                .addKeyValue("event", "dependency_call_failed")
                .addKeyValue("upstreamStatus", ex.status())
                .log("Falló la consulta a otro servicio");

        return problem(status,
                unavailable ? "DEPENDENCY_UNAVAILABLE" :
                        "DEPENDENCY_ERROR",
                "No se pudo consultar el servicio de órdenes o conductores",
                request);
    }

    @ExceptionHandler(UncheckedIOException.class)
    public ProblemDetail fileReadFailure(
            UncheckedIOException ex, HttpServletRequest request) {
        log.atError()
                .addKeyValue("event", "assignment_file_read_failed")
                .addKeyValue("path", request.getRequestURI())
                .setCause(ex)
                .log("No se pudo leer el archivo");

        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "FILE_READ_ERROR",
                "No se pudo procesar el archivo", request);
    }

    private ProblemDetail problem(
            HttpStatus status, String title, String detail,
            HttpServletRequest request) {
        ProblemDetail result =
                ProblemDetail.forStatusAndDetail(status, detail);
        result.setTitle(title);
        result.setInstance(URI.create(request.getRequestURI()));
        return result;
    }
}