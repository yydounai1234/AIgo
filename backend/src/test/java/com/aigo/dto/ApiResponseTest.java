package com.aigo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.success(data);

        assertTrue(response.getSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getError());
    }

    @Test
    void testSuccessWithNullData() {
        ApiResponse<String> response = ApiResponse.success(null);

        assertTrue(response.getSuccess());
        assertNull(response.getData());
        assertNull(response.getError());
    }

    @Test
    void testSuccessWithNoArgs() {
        ApiResponse<String> response = ApiResponse.success();

        assertTrue(response.getSuccess());
        assertNull(response.getData());
        assertNull(response.getError());
    }

    @Test
    void testErrorWithCodeAndMessage() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Error occurred";
        ApiResponse<String> response = ApiResponse.error(errorCode, errorMessage);

        assertFalse(response.getSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(errorCode, response.getError().getCode());
        assertEquals(errorMessage, response.getError().getMessage());
    }

    @Test
    void testErrorWithErrorCode() {
        ApiResponse<String> response = ApiResponse.error(ErrorCode.NOT_FOUND);

        assertFalse(response.getSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getError().getCode());
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.getError().getMessage());
    }

    @Test
    void testErrorWithErrorCodeAndCustomMessage() {
        String customMessage = "Custom error message";
        ApiResponse<String> response = ApiResponse.error(ErrorCode.BAD_REQUEST, customMessage);

        assertFalse(response.getSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getError().getCode());
        assertEquals(customMessage, response.getError().getMessage());
    }

    @Test
    void testConstructorWithAllArgs() {
        ErrorInfo errorInfo = new ErrorInfo("CODE", "Message");
        ApiResponse<Integer> response = new ApiResponse<>(false, null, errorInfo);

        assertFalse(response.getSuccess());
        assertNull(response.getData());
        assertEquals(errorInfo, response.getError());
    }

    @Test
    void testSettersAndGetters() {
        ApiResponse<String> response = new ApiResponse<>();
        ErrorInfo errorInfo = new ErrorInfo("ERROR_CODE", "Error message");
        
        response.setSuccess(true);
        response.setData("test");
        response.setError(errorInfo);

        assertTrue(response.getSuccess());
        assertEquals("test", response.getData());
        assertEquals(errorInfo, response.getError());
    }
}
