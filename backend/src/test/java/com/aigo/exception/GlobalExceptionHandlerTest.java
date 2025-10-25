package com.aigo.exception;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.NOT_FOUND, "Resource not found");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessExceptionBadRequest() {
        BusinessException exception = new BusinessException(ErrorCode.BAD_REQUEST, "Invalid input");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessExceptionUnauthorized() {
        BusinessException exception = new BusinessException(ErrorCode.UNAUTHORIZED, "Unauthorized access");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testHandleBusinessExceptionForbidden() {
        BusinessException exception = new BusinessException(ErrorCode.FORBIDDEN, "Access forbidden");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testHandleValidationException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("field"));
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("服务器内部错误", response.getBody().getMessage());
    }
}
