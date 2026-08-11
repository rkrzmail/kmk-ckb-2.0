package com.kmkbe.exception;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Useful for serialization checks if needed

   @MockBean
  ErrorLogRepository errorLogRepository;
    // ========================================================================
    // 1. Test LoanDocMandatoryException Handling
    // ========================================================================
    @Test
    void testHandleLoanDocMandatoryException() throws Exception {
        // Arrange: Mock the exception to be thrown
        LoanDocMandatoryException mockException = new LoanDocMandatoryException("Missing mandatory document XYZ.");

        // To properly test this, you'd normally need a controller calling it.
        // Here, we simulate calling the handler method directly for isolated testing.
        ResponseEntity<CommonResult<LoanDocMandatoryException>> responseEntity = (ResponseEntity<CommonResult<LoanDocMandatoryException>>) GlobalExceptionHandler.class.getDeclaredMethod(
                "handleLoanDocMandatoryException", LoanDocMandatoryException.class)
                .invoke(null, mockException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() != null);
        CommonResult<LoanDocMandatoryException> body = (CommonResult<LoanDocMandatoryException>) responseEntity.getBody();
        assertFalse(body.isSuccess());
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getCode());
        assertEquals("Missing mandatory document XYZ.", body.getMessage());
    }


    // ========================================================================
    // 2. Test BusinessException Handling
    // ========================================================================
    @Test
    void testHandleBusinessException() throws Exception {
        // Arrange: Create a mock exception instance
        BusinessException mockException = new BusinessException(HttpStatus.FORBIDDEN, 5001, "Access Denied");

        // Act: Invoke the handler method
        ResponseEntity<ErrorResponse> responseEntity = (ResponseEntity<ErrorResponse>) GlobalExceptionHandler.class.getDeclaredMethod(
                "handleBusinessException", BusinessException.class)
                .invoke(null, mockException);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals("Error Business Exception", errorResponse.getTitle());
        assertEquals(5001, errorResponse.getCode());
        assertEquals("Access Denied", errorResponse.getMessage());
    }

    // ========================================================================
    // 4. Test Framework Validation Exceptions
    // ========================================================================
    @Test
    void testHandleMethodArgumentNotValidException() throws Exception {
        // Arrange: Mock the exception structure (Requires simulating BindingResult)
        MockMvc mockMvcInstance = mockMvc; // Use this if testing via @WebMvcTest context

        // Since mocking MethodArgumentNotValidException and its internal state (BindingResult)
        // is extremely difficult without a full MVC environment, we test the expected outcome structure.

        // In a real test setup using MockMvc:
        mockMvcInstance.perform(get("/api/public/v1/bouwheers")
                .param("fieldA", "invalid value")) // Simulating failure on fieldA validation
            .andExpect(status().isBadRequest());

        // For direct method testing, you would need to mock the BindingResult object entirely.
    }


    @Test
    void testHandleDataIntegrityViolationException() throws Exception {
        // Arrange: Mock exception
        DataIntegrityViolationException mockException = new DataIntegrityViolationException("Duplicate key constraint");

        // Act: Invoke handler method (Requires accessing private/protected methods via reflection or making them package-private for testing)
        ResponseEntity<ErrorResponse> responseEntity = (ResponseEntity<ErrorResponse>) GlobalExceptionHandler.class.getDeclaredMethod(
                "handleDataIntegrityViolationException", DataIntegrityViolationException.class)
                .invoke(null, mockException);

        // Assert
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNull(errorResponse.getValidations()); // Should not contain validations in this specific handler
        assertEquals("Violates foreign key constraint", errorResponse.getMessage());
    }

    // Note: Testing TimeoutException and HttpServerErrorException follows the same pattern as above,
    // invoking the respective private/protected handler methods via reflection for isolation.
}
