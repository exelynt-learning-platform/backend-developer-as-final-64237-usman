package com.usman.resourcebooking.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    @DisplayName("handleValidationErrors returns 400 Bad Request with field errors")
    void handleValidationErrors_Returns400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("object", "field", "must not be null")));
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("/api/test", response.getBody().get("path"));
        Map<String, String> errors = (Map<String, String>) response.getBody().get("validationErrors");
        assertEquals("must not be null", errors.get("field"));
    }

    @Test
    @DisplayName("handleTypeMismatch returns 400 Bad Request")
    void handleTypeMismatch_Returns400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Parameter 'id' should be of type Unknown", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleMessageNotReadable returns 400 Bad Request")
    void handleMessageNotReadable_Returns400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON request or invalid data format", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleResourceNotFound returns 404 Not Found")
    void handleResourceNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource", "id", 1L);
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found with id : '1'", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleConflict returns 409 Conflict")
    void handleConflict_Returns409() {
        ConflictException ex = new ConflictException("Conflict message");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict message", response.getBody().get("message"));
    }
    
    @Test
    @DisplayName("handleDataIntegrityViolation returns 409 Conflict")
    void handleDataIntegrityViolation_Returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate key");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Cannot perform this action because the record is in use by another entity.", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleAccessDenied returns 403 Forbidden")
    void handleAccessDenied_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You do not have permission to perform this action", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleForbidden returns 403 Forbidden")
    void handleForbidden_Returns403() {
        ForbiddenException ex = new ForbiddenException("Forbidden message");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbidden(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden message", response.getBody().get("message"));
    }
    
    @Test
    @DisplayName("handleBadRequest returns 400 Bad Request")
    void handleBadRequest_Returns400() {
        BadRequestException ex = new BadRequestException("Bad request message");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request message", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleGeneric returns 500 Internal Server Error")
    void handleGeneric_Returns500() {
        Exception ex = new Exception("Internal error");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().get("message"));
    }
}
