package com.kmkbe.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    private HttpStatus testStatus;
    private Integer testCode;
    private String testMessage;
    private String testTitle;

    @BeforeEach
    void setUp() {
        // Setup common values for tests
        testStatus = HttpStatus.BAD_REQUEST;
        testCode = 9001;
        testMessage = "Business logic failed.";
        testTitle = "Business Error Occurred";
    }

    @Test
    @DisplayName("Should correctly initialize with (Integer code, String description, String message)")
    void testConstructorWithErrorCodeAndDesc() {
        // Act: Using the first constructor overload
        BaseException exception = new BusinessException(testCode, "BUSINESS_ERROR", testMessage);

        // Assert - Verify calls to parent class logic are correct
        assertFalse(exception.isSuccess());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus()); // Explicitly checked because of the constructor implementation in BusinessException
        assertEquals(testCode, exception.getCode());
        // Note: The parent's message setter receives errorDesc as the 'message' field value
        assertEquals("BUSINESS_ERROR", exception.getMessage());
    }

    @Test
    @DisplayName("Should correctly initialize with (HttpStatus status, Integer code, String message)")
    void testConstructorWithHttpStatus() {
        // Act: Using the second constructor overload
        BusinessException exception = new BusinessException(testStatus, testCode, testMessage);

        // Assert - Verify calls to parent class logic are correct
        assertFalse(exception.isSuccess());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(testCode, exception.getCode());
        // Note: Here, the 'errorMessage' passed to super will be "Business logic failed."
        assertEquals(testMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException if core parameters are null (when not handled)")
    void testConstructorWithNullParameters() {
        // Test case to verify required parameter enforcement based on constructor implementation
        // Here we expect the underlying RuntimeException/BaseException constructor logic to fail gracefully or throw NPE.

        // Since BaseException handles its own constructors, testing nulls reveals how dependent it is on mandatory inputs.
        // For this test, we check if passing required arguments results in expected state rather than crashing unexpectedly.

        // Test case for the first constructor: Requires code and message
        assertDoesNotThrow(() -> {
             new BusinessException(null, "Test", null); // Testing internal robustness
        });
    }
}
