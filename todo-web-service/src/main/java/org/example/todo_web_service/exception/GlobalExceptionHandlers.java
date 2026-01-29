package org.example.todo_web_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandlers extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
        var body = ApiErrorResponse.of(
                ex.status().value(),
                ex.status().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.status()).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        var body = ApiErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // DTO @Valid errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        var body = ApiErrorResponse.of(
                400, "Bad Request", "Validation failed",
                extractPath(request),
                Map.of("validationErrors", fieldErrors)
        );

        return ResponseEntity.badRequest().body(body);
    }

    // Path/query param bean validation (@Positive, @Size, etc)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                400, "Bad Request", "Validation failed",
                request.getRequestURI(),
                Map.of("violations", ex.getConstraintViolations().stream().map(v -> Map.of(
                        "property", String.valueOf(v.getPropertyPath()),
                        "message", v.getMessage()
                )).toList())
        );

        return ResponseEntity.badRequest().body(body);
    }

    // Malformed JSON / invalid enum value -> 400
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        var body = ApiErrorResponse.of(
                400, "Bad Request", "Malformed JSON request",
                extractPath(request)
        );
        return ResponseEntity.badRequest().body(body);
    }

    // missing request param
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        var body = ApiErrorResponse.of(
                400, "Bad Request", ex.getMessage(),
                extractPath(request)
        );
        return ResponseEntity.badRequest().body(body);
    }

    // wrong type in param/path variable
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                400, "Bad Request",
                "Invalid value for parameter '" + ex.getName() + "'",
                request.getRequestURI(),
                Map.of("rejectedValue", ex.getValue())
        );
        return ResponseEntity.badRequest().body(body);
    }

    // fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        var body = ApiErrorResponse.of(
                500, "Internal Server Error", "Unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String extractPath(org.springframework.web.context.request.WebRequest request) {
        String desc = request.getDescription(false);
        return (desc != null && desc.startsWith("uri=")) ? desc.substring(4) : "";
    }
}