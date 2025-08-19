package sg.edu.ntu.garang_guni_backend.exceptions;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

public class ProblemDetailBuilder {

    private ProblemDetail problemDetail;

    private ProblemDetailBuilder(HttpStatusCode status) {
        this.problemDetail = ProblemDetail.forStatus(status);
    }

    private ProblemDetailBuilder(HttpStatusCode status, String detail) {
        this.problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    }

    public static ProblemDetailBuilder forStatus(HttpStatus status) {
        return new ProblemDetailBuilder(status);
    }

    public static ProblemDetailBuilder forStatusAndDetail(HttpStatusCode status, String detail) {
        return new ProblemDetailBuilder(status, detail);
    }

    public ProblemDetailBuilder withTitle(String title) {
        this.problemDetail.setTitle(title);
        return this;
    }

    public ProblemDetailBuilder withDetail(String detail) {
        this.problemDetail.setDetail(detail);
        return this;
    }

    public ProblemDetailBuilder withErrorType(ErrorType type) {
        this.problemDetail.setType(type.getUri());
        return this;
    }

    public ProblemDetailBuilder withProperty(String name, Object value) {
        this.problemDetail.setProperty(name, value);
        return this;
    }

    public ProblemDetailBuilder withInstance(String uri) {
        this.problemDetail.setInstance(URI.create(uri));
        return this;
    }

    public ProblemDetail build() {
        return this.problemDetail;
    }

}
