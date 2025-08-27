package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
public class OtpServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private OtpServiceImpl otpService;

    private static final String VALID_OTP = "VALID12345";
    private static final String INCORRECT_OTP = "WRONGOTP12";
    private UUID userId;
    private String cacheKey;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        cacheKey = "otp:" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @DisplayName("Generate And Store OTP - Successful")
    @Test
    void generateAndStoreOtpTest() {
        String otp = otpService.generateAndStoreOtp(userId);

        assertNotNull(otp);
        assertEquals(10, otp.length(), "OTP should have length 10");

        verify(valueOps, times(1))
                .set(eq(cacheKey), eq(otp), eq(Duration.ofMinutes(5)));
    }

    @DisplayName("Validating OTP - Successful")
    @Test
    void isOtpValidTrueTest() {
        when(valueOps.get(cacheKey)).thenReturn(VALID_OTP);

        boolean isOtpValid = otpService.isOtpValid(userId, VALID_OTP);

        assertTrue(isOtpValid);
        verify(valueOps, times(1)).get(cacheKey);
    }

    @DisplayName("Validating OTP - Invalid OTP")
    @Test
    void isOtpValidFalseTest() {
        when(valueOps.get(cacheKey)).thenReturn(VALID_OTP);

        boolean isOtpValid = otpService.isOtpValid(userId, INCORRECT_OTP);

        assertFalse(isOtpValid);
        verify(valueOps, times(1)).get(cacheKey);
    }

    @DisplayName("Validating OTP - No Otp in Redis")
    @Test
    void isOtpValidNoValueTest() {
        when(valueOps.get(cacheKey)).thenReturn(null);

        boolean result = otpService.isOtpValid(userId, VALID_OTP);

        assertFalse(result);
        verify(valueOps, times(1)).get(cacheKey);
    }
}
