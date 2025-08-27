package sg.edu.ntu.garang_guni_backend.configs;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import sg.edu.ntu.garang_guni_backend.configs.security.services.JwtService;

@Configuration
@Setter
@Getter
@Slf4j
@ConfigurationProperties(prefix = "security.jwt")
public class JwtConfig {
    private String privateKey;
    private String publicKey;
    private Duration ttl;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        log.info("Loading RSA private key from environment variable");

        String keyContent = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(spec);
        log.info("✅ RSA private key loaded successfully");
        return rsaPrivateKey;
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        log.info("Loading RSA public key from environment variable");

        String keyContent = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(spec);
        log.info("✅ RSA public key loaded successfully");
        return rsaPublicKey;
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAPrivateKey rsaPrivateKey, RSAPublicKey rsaPublicKey) {
        RSAKey jwk = new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build();

        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    @Bean
    public JwtService jwtService(@Value("${spring.application.name}") String appName,
            JwtEncoder jwtEncoder) {

        return new JwtService(appName, ttl, jwtEncoder);
    }

}
