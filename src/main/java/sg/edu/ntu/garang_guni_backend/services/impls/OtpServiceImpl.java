package sg.edu.ntu.garang_guni_backend.services.impls;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sg.edu.ntu.garang_guni_backend.services.OtpService;

@Service
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String OTP_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int OTP_LEN = 10;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private RedisTemplate<String, String> redis;

    public OtpServiceImpl(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public String generateAndStoreOtp(UUID id) {
        String otp = generateOtp(OTP_CHARACTERS, OTP_LEN);
        String cacheKey = getCacheKey(id);

        redis.opsForValue().set(cacheKey, otp, OTP_EXPIRY);

        return otp;
    }

    @Override
    public boolean isOtpValid(UUID id, String otp) {
        String cacheKey = getCacheKey(id);
        boolean isOtpValid = Objects.equals(redis.opsForValue().get(cacheKey), otp);

        return isOtpValid;
    }

    @Override
    public void deleteOtp(UUID id) {
        String cacheKey = getCacheKey(id);
        redis.delete(cacheKey);
    }

    private String getCacheKey(UUID id) {
        return "otp:%s".formatted(id);
    }

    private String generateOtp(String availableChars, int otpLen) {
        StringBuilder otp = new StringBuilder(otpLen);
        int charsLen = availableChars.length();
        for (int i = 0; i < otpLen; i++) {
            int idx = SECURE_RANDOM.nextInt(charsLen);
            otp.append(availableChars.charAt(idx));
        }
        return otp.toString();
    }
}