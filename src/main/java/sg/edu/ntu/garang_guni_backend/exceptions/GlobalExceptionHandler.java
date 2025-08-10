package sg.edu.ntu.garang_guni_backend.exceptions;

import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sg.edu.ntu.garang_guni_backend.exceptions.booking.BookingNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.image.ImageNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.image.ImageUtilsException;
import sg.edu.ntu.garang_guni_backend.exceptions.item.ItemNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.location.LocationNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles not found exception.
     *
     * @param exception the exception that was thrown
     * @return a ResponseEntity containing the error message and a 404 Not Found
     *         status
     */
    // @formatter:off
    @ExceptionHandler({
        UserNotFoundException.class,
        UsernameNotFoundException.class,
        ContactNotFoundException.class,
        ImageNotFoundException.class,
        ItemNotFoundException.class,
        ScrapDealerNotFoundException.class,
        AvailabilityNotFoundException.class,
        LocationNotFoundException.class,
        BookingNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleResourceException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // @formatter:on
    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDateException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles the UserExistsException when a user attempts to register with an
     * email that already
     * exists in the system.
     *
     * @param exception the UserExistsException thrown when a duplicate user is
     *                  detected
     * @return a ResponseEntity containing the error message and a 409 CONFLICT
     *         status
     */
    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExistsException(UserExistsException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ContactNotProcessingException.class)
    public ResponseEntity<ErrorResponse> handleContactNotProcessingException(
            ContactNotProcessingException ex) {
        logger.error("Error: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ImageUtilsException.class)
    public ResponseEntity<ErrorResponse> handleImageCompressionException(ImageUtilsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<ObjectError> validationErrors = ex.getBindingResult().getAllErrors();
        StringBuilder sb = new StringBuilder();
        for (ObjectError error : validationErrors) {
            sb.append(error.getDefaultMessage());
            sb.append(". ");
        }
        ErrorResponse errorResponse = new ErrorResponse(sb.toString(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        String errorMsg = ex.getMessage();
        String re = "Detail: ([^]]+)";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(errorMsg);
        String detailMsg = "";
        if (matcher.find()) {
            detailMsg = matcher.group(1).trim();
        } else {
            detailMsg = "Unknown unique constraint has been violated";
        }

        ErrorResponse errorMessage = new ErrorResponse(
                "A data integrity error occurred: " + detailMsg, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Malformed JSON request", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles any general exceptions thrown during the application's execution. If
     * the exception is
     * of type BadCredentialsException, it returns an unauthorized response.
     *
     * @param exception the exception that was thrown
     * @return a ResponseEntity containing a customized error message and the
     *         appropriate HTTP
     *         status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error(ex.getMessage(), ex);

        if (ex instanceof BadCredentialsException) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "The email or password is incorrect", LocalDateTime.now());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        ErrorResponse errorResponse = new ErrorResponse(
                "An error occurred. Please contact support.", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid username or password", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
