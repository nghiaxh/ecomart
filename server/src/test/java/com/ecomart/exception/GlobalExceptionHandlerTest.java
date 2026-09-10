package com.ecomart.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock HttpServletRequest request;

    public static void sampleEndpoint(String amount) {
    }

    private void stubPath() {
        when(request.getRequestURI()).thenReturn("/api/example");
    }

    @Test
    void validationErrorsReturnFirstFieldMessage() throws NoSuchMethodException {
        stubPath();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "amount", "must not be null"));
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getMethod("sampleEndpoint", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("amount must not be null", response.getBody().message());
        assertEquals("/api/example", response.getBody().path());
    }

    @Test
    void badCredentialsReturnGenericMessage() {
        stubPath();
        BadCredentialsException ex = new BadCredentialsException("bad password");

        ResponseEntity<ApiError> response = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().message());
    }

    @Test
    void accessDeniedReturnsForbidden() {
        stubPath();
        AccessDeniedException ex = new AccessDeniedException("no permission");

        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().message());
    }

    @Test
    void genericExceptionReturnsInternalServerError() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/example");

        ResponseEntity<ApiError> response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An internal server error occurred", response.getBody().message());
    }
}