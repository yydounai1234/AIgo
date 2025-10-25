package com.aigo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.success(data);

        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getMessage());
    }

    @Test
    void testSuccessWithNullData() {
        ApiResponse<String> response = ApiResponse.success(null);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void testError() {
        String errorMessage = "Error occurred";
        ApiResponse<String> response = ApiResponse.error(errorMessage);

        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testBuilder() {
        ApiResponse<Integer> response = ApiResponse.<Integer>builder()
                .success(true)
                .data(42)
                .message("Success message")
                .build();

        assertTrue(response.isSuccess());
        assertEquals(42, response.getData());
        assertEquals("Success message", response.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData("test");
        response.setMessage("message");

        assertTrue(response.isSuccess());
        assertEquals("test", response.getData());
        assertEquals("message", response.getMessage());
    }
}
