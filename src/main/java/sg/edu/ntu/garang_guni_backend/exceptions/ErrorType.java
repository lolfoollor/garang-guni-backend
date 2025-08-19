package sg.edu.ntu.garang_guni_backend.exceptions;

import static java.net.URI.create;

import java.net.URI;
import lombok.Getter;

@Getter
public enum ErrorType {
    UNAUTHORIZED(create("errors/unauthorized")),
    FORBIDDEN(create("errors/forbidden")),
    ACCOUNT_UNAVAILABLE(create("errors/account-unavailable")),
    DB_CONSTRAINT_VIOLATION(create("errors/db-constraint-violation")),
    REQUEST_VALIDATION_FAILED(create("errors/request-validation-failed")),
    RESOURCE_ALREADY_EXISTS(create("errors/resource-already-exists")),
    EMAIL_VERIFICATION_REQUIRED(create("errors/email-verification-required")),
    EMAIL_VERIFICATION_FAILED(create("errors/email-verification-failed")),
    EMAIL_ALREADY_VERIFIED(create("errors/email-already-verified")),
    UNKNOWN_SERVER_ERROR(create("errors/unknown-server-error")),
    BAD_REQUEST(create("errors/bad-request")),
    NOT_FOUND(create("errors/not-found")),
    METHOD_NOT_ALLOWED(create("errors/method-not-allowed")),
    UNSUPPORTED_MEDIA_TYPE(create("errors/unsupported-media-type")),
    PAYLOAD_TOO_LARGE(create("errors/payload-too-large")),
    SERVICE_UNAVAILABLE(create("errors/service-unavailable"));

    private final URI uri;

    ErrorType(URI uri) {
        this.uri = uri;
    }
}