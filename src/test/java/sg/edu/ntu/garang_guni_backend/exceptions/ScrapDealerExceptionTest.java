package sg.edu.ntu.garang_guni_backend.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import sg.edu.ntu.garang_guni_backend.services.ScrapDealerService;

class ScrapDealerExceptionTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private ScrapDealerService scrapDealerService;

    private static final String REQUEST_URI = "/location/123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test Handling ScrapDealer Not Found Exception")
    void testHandleScrapDealerNotFoundException() {
        String errorMsg = "Scrap dealer not found";
        ScrapDealerNotFoundException exception = new ScrapDealerNotFoundException(
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
    @DisplayName("Test Handling Unauthorized Access Exception for ScrapDealer")
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
    @DisplayName("Test Handling Generic Exception for ScrapDealer")
    void testHandleGenericException() {
        String errorMsg = "Unexpected error occurred";
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
