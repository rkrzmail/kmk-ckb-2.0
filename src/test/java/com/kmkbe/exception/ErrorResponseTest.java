package com.kmkbe.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Initialize ObjectMapper to test JSON serialization behavior (for testing @JsonInclude)
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should build successfully using Builder pattern")
    void testBuilderInitialization() {
        // Arrange
        ErrorValidationResponse validation1 = ErrorValidationResponse.builder().propertyName("fieldA").errorMessage("Error A").build();
        List<ErrorValidationResponse> validations = Arrays.asList(validation1);

        // Act
        ErrorResponse response = ErrorResponse.builder()
                .code(500)
                .title("Server Error")
                .message("Could not process request.")
                .validations(validations)
                // Assume BaseResponse has a field that gets set by default or here
                .build();

        // Assert
        assertEquals(500, response.getCode());
        assertEquals("Server Error", response.getTitle());
        assertTrue(response.getValidations().contains(validation1));
    }

    @Test
    @DisplayName("Should handle null validations list correctly (serialization test)")
    void testSerializationWhenValidationsIsNull() throws Exception {
        // Arrange: Build a response where 'validations' is null, simulating JSON exclusion
        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .title("Client Error")
                .message("Input validation failed.")
                // Intentionally omitting or setting to null for validations
                .build();

        // Mocking the BaseResponse state if necessary, but focusing on 'validations' exclusion
        response.setValidations(null);

        // Act: Serialize to JSON string
        String json = objectMapper.writeValueAsString(response);

        // Assert: Check if "validations" key is absent from the resulting JSON string
        assertFalse(json.contains("\"validations\":"));
    }

    @Test
    @DisplayName("Should correctly serialize when validations list is empty")
    void testSerializationWhenValidationsIsEmpty() throws Exception {
        // Arrange
        List<ErrorValidationResponse> emptyValidations = Collections.emptyList();
        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .title("Client Error")
                .message("No specific validation failures.")
                .validations(emptyValidations)
                .build();

        // Act: Serialize to JSON string
        String json = objectMapper.writeValueAsString(response);

        // Assert: Check if the array structure is present but empty (which it should be if non-null)
        assertTrue(json.contains("\"validations\":[]"));
    }
}
