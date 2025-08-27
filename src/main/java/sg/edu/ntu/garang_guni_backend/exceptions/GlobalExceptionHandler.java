package sg.edu.ntu.garang_guni_backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sg.edu.ntu.garang_guni_backend.exceptions.booking.BookingNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.EmailAlreadyVerifiedException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.EmailNotVerifiedException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.InvalidTokenException;
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
    public ProblemDetail handleResourceException(
            Exception ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Resource not found")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.NOT_FOUND)
                .withInstance(request.getRequestURI())
                .build();
    }

    // @formatter:on
    @ExceptionHandler(InvalidDateException.class)
    public ProblemDetail handleInvalidDate(
            InvalidDateException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ProblemDetailBuilder
                .forStatus(status)
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.BAD_REQUEST)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ProblemDetail handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        return ProblemDetailBuilder
                .forStatus(status)
                .withErrorType(ErrorType.FORBIDDEN)
                .withInstance(request.getRequestURI())
                .build();
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
    public ProblemDetail handleUserExistsException(
            UserExistsException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        return ProblemDetailBuilder
                .forStatus(status)
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.RESOURCE_ALREADY_EXISTS)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ContactNotProcessingException.class)
    public ProblemDetail handleContactNotProcessingException(
            ContactNotProcessingException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ProblemDetailBuilder
                .forStatus(status)
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.UNKNOWN_SERVER_ERROR)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ImageUtilsException.class)
    public ProblemDetail handleImageCompressionException(
            ImageUtilsException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());

        ErrorType errorType = switch (status) {
            case BAD_REQUEST -> ErrorType.BAD_REQUEST;
            case UNSUPPORTED_MEDIA_TYPE -> ErrorType.UNSUPPORTED_MEDIA_TYPE;
            case PAYLOAD_TOO_LARGE -> ErrorType.PAYLOAD_TOO_LARGE;
            default -> ErrorType.UNKNOWN_SERVER_ERROR;
        };

        return ProblemDetailBuilder
                .forStatus(status)
                .withDetail(ex.getMessage())
                .withErrorType(errorType)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList();

        return ProblemDetailBuilder
                .forStatus(status)
                .withDetail("One or more validation errors occurred")
                .withErrorType(ErrorType.REQUEST_VALIDATION_FAILED)
                .withInstance(request.getRequestURI())
                .withProperty("errors", errors)
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        String errorMsg = ex.getMessage();
        String re = "Detail: ([^]]+)";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(errorMsg);
        String detailMsg = matcher.find()
                ? matcher.group(1).trim()
                : "Unknown data integrity constraint has been violated";

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Data integrity violation")
                .withDetail(detailMsg)
                .withErrorType(ErrorType.DB_CONSTRAINT_VIOLATION)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Malformed JSON request")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.BAD_REQUEST)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logger.warn("Validation failed: {}", ex.getMessage());

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Validation failed")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.REQUEST_VALIDATION_FAILED)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Authentication failed")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.UNAUTHORIZED)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerifiedException(
            EmailNotVerifiedException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Email verification required")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.UNAUTHORIZED)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ProblemDetail handleEmailAlreadyVerifiedException(
            EmailAlreadyVerifiedException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Email verification not required")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.EMAIL_ALREADY_VERIFIED)
                .withInstance(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidTokenException(
            InvalidTokenException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Email validation failed")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.EMAIL_VERIFICATION_FAILED)
                .withInstance(request.getRequestURI())
                .build();
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
    public ProblemDetail handleException(
            Exception ex,
            HttpServletRequest request) {
        logger.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof BadCredentialsException badCredentialsException) {
            return handleBadCredentials(badCredentialsException, request);
        }

        return ProblemDetailBuilder
                .forStatus(status)
                .withTitle("Something went wrong")
                .withDetail(ex.getMessage())
                .withErrorType(ErrorType.UNKNOWN_SERVER_ERROR)
                .withInstance(request.getRequestURI())
                .build();
    }
}
