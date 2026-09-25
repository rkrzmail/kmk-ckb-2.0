package com.kmkbe.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ErrorValidationResponseTest {

    private String mockBaseId;

    @BeforeEach
    void setUp() {
        // Setup a predictable unique ID for testing BaseResponse inheritance
        mockBaseId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("Should initialize and retrieve all fields correctly via Builder")
    void testBuilderInitialization() {
        // Arrange: Build the object using builder pattern
        ErrorValidationResponse response = ErrorValidationResponse.builder()
                .propertyName("fieldA")
                .errorMessage("This field failed validation.")
                // Assuming BaseResponse has a way to set its unique ID/key for testing inheritance state
                // If BaseResponse requires an ID, you might need to mock/provide it here if the builder doesn't support it.
                // For this test, we focus on fields defined in ErrorValidationResponse itself.
                .build();

        // Manually setting a base property that should be part of inheritance for verification purposes
        // If BaseResponse has methods like setId(), use them here:
        // response.setId(mockBaseId);

        // Assert
        assertEquals("fieldA", response.getPropertyName());
        assertEquals("This field failed validation.", response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle null or empty inputs without throwing exceptions")
    void testNullAndEmptyInputs() {
        // Act: Build object with minimum/null inputs
        ErrorValidationResponse response = ErrorValidationResponse.builder()
                .propertyName(null)
                .errorMessage("")
                .build();

        // Assert
        assertNull(response.getPropertyName());
        assertEquals("", response.getErrorMessage());
    }

    @Test
    @DisplayName("Should correctly use getters for all defined fields")
    void testGetters() {
        // Arrange
        ErrorValidationResponse response = ErrorValidationResponse.builder()
                .propertyName("someField")
                .errorMessage("Check this.")
                .build();

        // Act & Assert
        assertNotNull(response);
        assertEquals("someField", response.getPropertyName());
        assertEquals("Check this.", response.getErrorMessage());
    }
}
