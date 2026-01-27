package org.example.user_web_service.exception.handlers;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.example.user_web_service.exception.ApiErrorResponse;
import org.example.user_web_service.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandlers extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {

        ApiErrorResponse body = ApiErrorResponse.of(
                ex.status().value(),
                ex.status().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.status()).body(body);
    }

    // -------- Validation (DTO @Valid) --------
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {

        Map<String, Object> details = new LinkedHashMap<>();
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        details.put("validationErrors", fieldErrors);

        String path = extractPath(request);
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                path,
                details
        );

        return ResponseEntity.badRequest().body(body);
    }

    // -------- Bean Validation (path/query params) --------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("violations", ex.getConstraintViolations().stream().map(v -> Map.of(
                "property", String.valueOf(v.getPropertyPath()),
                "message", v.getMessage()
        )).toList());

        ApiErrorResponse body = ApiErrorResponse.of(
                400, "Bad Request", "Validation failed", request.getRequestURI(), details
        );

        return ResponseEntity.badRequest().body(body);
    }

    // -------- Bad JSON / unreadable body --------
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {

        String path = extractPath(request);
        ApiErrorResponse body = ApiErrorResponse.of(
                400, "Bad Request", "Malformed JSON request", path
        );

        return ResponseEntity.badRequest().body(body);
    }

    // -------- Missing params --------
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {

        String path = extractPath(request);
        ApiErrorResponse body = ApiErrorResponse.of(
                400, "Bad Request", ex.getMessage(), path
        );

        return ResponseEntity.badRequest().body(body);
    }

    // -------- Type mismatch (e.g. id=abc) --------
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        String msg = "Invalid value for parameter '" + ex.getName() + "'";
        ApiErrorResponse body = ApiErrorResponse.of(
                400, "Bad Request", msg, request.getRequestURI(),
                Map.of("rejectedValue", ex.getValue())
        );

        return ResponseEntity.badRequest().body(body);
    }

    // -------- 404 (if any other exception not handled) --------
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {

        String path = extractPath(request);
        ApiErrorResponse body = ApiErrorResponse.of(
                404, "Not Found", "No handler for " + ex.getHttpMethod() + " " + ex.getRequestURL(), path
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // -------- Auth / Security --------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.of(
                401, "Unauthorized", "Invalid credentials", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.of(
                403, "Forbidden", "Access is denied", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwt(
            JwtException ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.of(
                401, "Unauthorized", "Invalid or expired token", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // -------- App / Domain "not found" --------
    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.of(
                404, "Not Found", ex.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // -------- DB constraint issues --------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.of(
                409, "Conflict", "Database constraint violation", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // -------- Fallback: never leak stack traces to clients --------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(
            Exception ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.of(
                500, "Internal Server Error", "Unexpected error occurred", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String extractPath(org.springframework.web.context.request.WebRequest request) {

        String desc = request.getDescription(false);
        if (desc != null && desc.startsWith("uri=")) {
            return desc.substring(4);
        }

        return "";
    }
}