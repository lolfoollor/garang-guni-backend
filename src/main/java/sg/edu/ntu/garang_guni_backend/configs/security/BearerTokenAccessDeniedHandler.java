package sg.edu.ntu.garang_guni_backend.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import sg.edu.ntu.garang_guni_backend.exceptions.ErrorType;
import sg.edu.ntu.garang_guni_backend.exceptions.ProblemDetailBuilder;

@Component
@Slf4j
public class BearerTokenAccessDeniedHandler implements AccessDeniedHandler {
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final ObjectMapper objectMapper;

    public BearerTokenAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;

        log.info("{}: {}", status.getReasonPhrase(), accessDeniedException.getMessage());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                ProblemDetailBuilder.forStatus(status)
                        .withErrorType(ErrorType.FORBIDDEN)
                        .withInstance(request.getRequestURI())
                        .build());
    }
}
