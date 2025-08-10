package sg.edu.ntu.garang_guni_backend.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sg.edu.ntu.garang_guni_backend.exceptions.ErrorType;
import sg.edu.ntu.garang_guni_backend.exceptions.ProblemDetailBuilder;

@Component
@Slf4j
public class BearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private ObjectMapper objectMapper;

    public BearerTokenAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        log.info("{}: {}", status.getReasonPhrase(), authException.getMessage());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                "Bearer error=\"invalid_token\", error_description=\"" + authException.getMessage()
                        + "\", error_uri=\"https://tools.ietf.org/html/rfc6750#section-3.1\"");

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                ProblemDetailBuilder.forStatusAndDetail(status, authException.getMessage())
                        .withErrorType(ErrorType.UNAUTHORIZED)
                        .withInstance(request.getRequestURI())
                        .build());
    }
}
