package com.spareparts.spareparts_backend.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler  {
    // ================= CUSTOM & SPECIFIC EXCEPTIONS =================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        log.warn("Email conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    // ================= VALIDATION & STATE EXCEPTIONS =================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Business logic violation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Transaction Declined", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for request");
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("timestamp", LocalDateTime.now());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        response.put("message", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ================= SPRING FRAMEWORK EXCEPTIONS =================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.error("Response status exception: {} - Reason: {}", ex.getStatusCode(), ex.getReason());
        return buildErrorResponse(
                (HttpStatus) ex.getStatusCode(),
                "Request Error",
                ex.getReason() != null ? ex.getReason() : "Operation failed"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.error("Security violation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", "Access Denied: You do not have permission.");
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonProcessing(JsonProcessingException ex) {
        log.error("JSON processing error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON", "Error parsing request data.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' expects type '%s'", ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Parameter", message);
    }

    // ================= GLOBAL FALLBACK =================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex) {
        // 3. Replace printStackTrace with robust logging
        log.error("CRITICAL ERROR: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
    }

    // ================= HELPER METHOD =================

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String error, String message) {
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                error,
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, status);
    }
}
