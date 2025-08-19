package sg.edu.ntu.garang_guni_backend.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

class AvailabilityExceptionTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private static final String REQUEST_URI = "/availability/123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test Handling Availability Not Found Exception")
    void testHandleAvailabilityNotFoundException() {
        String errorMsg = "Availability not found";
        AvailabilityNotFoundException exception = new AvailabilityNotFoundException(
                errorMsg);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);

        ProblemDetail response = globalExceptionHandler
                .handleResourceException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals(errorMsg, response.getDetail());
        assertEquals(REQUEST_URI, response.getInstance().toString());
    }

    @Test
    @DisplayName("Test Handling Invalid Date Exception")
    void testHandleInvalidDateException() {
        String errorMsg = "Invalid date provided";
        InvalidDateException exception = new InvalidDateException(errorMsg);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);

        ProblemDetail response = globalExceptionHandler
                .handleInvalidDate(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(errorMsg, response.getDetail());
        assertEquals(REQUEST_URI, response.getInstance().toString());
    }

    @Test
    @DisplayName("Test Handling Unauthorized Access Exception for Availability")
    void testHandleUnauthorizedAccessException() {
        String errorMsg = "Unauthorized access";
        UnauthorizedAccessException exception = new UnauthorizedAccessException(
                errorMsg);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);

        ProblemDetail response = globalExceptionHandler
                .handleUnauthorizedAccess(exception, request);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals(REQUEST_URI, response.getInstance().toString());
    }

    @Test
    @DisplayName("Test Handling Generic Exception for Availability")
    void testHandleGenericException() {
        String errorMsg = "An error occurred. Please contact support.";
        Exception exception = new Exception(errorMsg);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);

        ProblemDetail response = globalExceptionHandler
                .handleException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals(errorMsg, response.getDetail());
        assertEquals(REQUEST_URI, response.getInstance().toString());
    }
}
