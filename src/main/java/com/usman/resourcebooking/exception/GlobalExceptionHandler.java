package com.usman.resourcebooking.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Validation (400)

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage()
                                                                : "Invalid value",
                                                (first, second) -> first));

                Map<String, Object> body = buildBase(HttpStatus.BAD_REQUEST, "Validation failed", request);
                body.put("validationErrors", fieldErrors);
                return ResponseEntity.badRequest().body(body);
        }

        // BadRequest (400)

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<Map<String, Object>> handleBadRequest(
                        BadRequestException ex,
                        HttpServletRequest request) {

                return ResponseEntity.badRequest()
                                .body(buildBase(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                return ResponseEntity.badRequest()
                                .body(buildBase(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                String message = String.format("Parameter '%s' should be of type %s",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");
                return ResponseEntity.badRequest()
                                .body(buildBase(HttpStatus.BAD_REQUEST, message, request));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return ResponseEntity.badRequest()
                                .body(buildBase(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid data format",
                                                request));
        }

        // Unauthorized (401)

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<Map<String, Object>> handleUnauthorized(
                        UnauthorizedException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(buildBase(HttpStatus.UNAUTHORIZED, ex.getMessage(), request));
        }

        @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
        public ResponseEntity<Map<String, Object>> handleAuthenticationException(
                        org.springframework.security.core.AuthenticationException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(buildBase(HttpStatus.UNAUTHORIZED, "Invalid username or password", request));
        }

        // Forbidden (403)

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<Map<String, Object>> handleForbidden(
                        ForbiddenException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(buildBase(HttpStatus.FORBIDDEN, ex.getMessage(), request));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, Object>> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(buildBase(HttpStatus.FORBIDDEN,
                                                "You do not have permission to perform this action", request));
        }

        // Not Found (404)

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleResourceNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildBase(HttpStatus.NOT_FOUND, ex.getMessage(), request));
        }

        // Conflict (409)

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<Map<String, Object>> handleConflict(
                        ConflictException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildBase(HttpStatus.CONFLICT, ex.getMessage(), request));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalState(
                        IllegalStateException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildBase(HttpStatus.CONFLICT, ex.getMessage(), request));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildBase(HttpStatus.CONFLICT,
                                                "Cannot perform this action because the record is in use by another entity.",
                                                request));
        }

        // Catch-all (500)

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(buildBase(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "An unexpected error occurred. Please try again later.", request));
        }

        // Helper

        private Map<String, Object> buildBase(HttpStatus status, String message,
                        HttpServletRequest request) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", Instant.now().toString());
                body.put("status", status.value());
                body.put("error", status.getReasonPhrase());
                body.put("message", message);
                body.put("path", request.getRequestURI());
                return body;
        }
}
