package sg.edu.ntu.garang_guni_backend.configs.security.services;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import sg.edu.ntu.garang_guni_backend.dtos.JwtUserDetails;

@RequiredArgsConstructor
public class JwtService {

    private final String issuer;

    private final Duration ttl;

    private final JwtEncoder jwtEncoder;

    public String generateToken(final JwtUserDetails userDetails) {
        final JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userDetails.email())
                .issuer(issuer)
                .expiresAt(Instant.now().plus(ttl))
                .claim("id", userDetails.id())
                .claim("username", userDetails.username())
                .claim("roles", userDetails.roles())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
