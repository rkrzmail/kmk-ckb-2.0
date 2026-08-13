package com.kmkbe.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class BaseExceptionTest {

    private HttpStatus testStatus;
    private Integer testCode;
    private String testMessage;
    private String testTitle;

    @BeforeEach
    void setUp() {
        // Setup common values for tests
        testStatus = HttpStatus.BAD_REQUEST;
        testCode = 4001;
        testMessage = "Error details provided.";
        testTitle = "Validation Failed";
    }

    @Test
    @DisplayName("Should initialize correctly using constructor (false, status, code, msg, title)")
    void testConstructorWithErrorState() {
        // Act: Using the first constructor overload
        BaseException exception = new BaseException(testStatus, testCode, testMessage, testTitle);

        // Assert
        assertFalse(exception.isSuccess());
        assertEquals(testStatus, exception.getHttpStatus());
        assertEquals(testCode, exception.getCode());
        assertEquals(testMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should initialize correctly using constructor (true, status, code, msg, title)")
    void testConstructorWithSuccessState() {
        // Act: Using the second constructor overload to simulate a success path if needed later,
        // though base exception usually implies failure. We test its initialization capability.
        BaseException exception = new BaseException(true, HttpStatus.OK, 200, "Operation successful", "Info");

        // Assert
        assertTrue(exception.isSuccess());
        assertEquals(HttpStatus.OK, exception.getHttpStatus());
        assertEquals(200, exception.getCode());
    }

    @Test
    @DisplayName("Should handle null inputs gracefully for optional fields")
    void testConstructorWithNullInputs() {
        // Act: Test with various nulls to ensure no NullPointerException occurs
        BaseException exception = new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, null, null, "System Error");

        // Assert
        assertFalse(exception.isSuccess());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertNull(exception.getCode());
        assertNull(exception.getMessage());
    }
}
