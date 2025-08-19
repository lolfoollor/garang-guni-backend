package sg.edu.ntu.garang_guni_backend.configs.security.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import sg.edu.ntu.garang_guni_backend.dtos.JwtUserDetails;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private Jwt jwt;

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;
    private JwtUserDetails userDetails;
    private String issuer;
    private Duration ttl;

    @BeforeEach
    void setUp() {
        issuer = "test-issuer";
        ttl = Duration.ofMinutes(1);
        jwtService = new JwtService(issuer, ttl, jwtEncoder);

        userDetails = new JwtUserDetails(
                UUID.randomUUID(),
                "testuser",
                "testuser@example.com",
                List.of("USER"));
    }

    @Test
    void generateToken_returnsExpectedToken() {
        String expectedTokenValue = "dummy.jwt.token";
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor
                .forClass(JwtEncoderParameters.class);
        when(jwtEncoder.encode(captor.capture())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn(expectedTokenValue);

        String actualToken = jwtService.generateToken(userDetails);

        assertEquals(expectedTokenValue, actualToken);

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertEquals(userDetails.email(), claims.getSubject());
        assertEquals(issuer, claims.getClaim("iss"));
        assertEquals(userDetails.id(), claims.getClaim("id"));
        assertEquals(userDetails.username(), claims.getClaim("username"));
        assertEquals(userDetails.roles(), claims.getClaim("roles"));

        Instant expectedExpiry = Instant.now().plus(Duration.ofMinutes(1));
        Instant actualExpiry = claims.getExpiresAt();
        long secondsDiff = Math.abs(expectedExpiry.getEpochSecond() - actualExpiry.getEpochSecond());
        assert (secondsDiff < 5);
    }
}
