package sg.edu.ntu.garang_guni_backend.services;

import java.util.UUID;

public interface OtpService {

    String generateAndStoreOtp(UUID id);

    boolean isOtpValid(UUID id, String otp);

    void deleteOtp(UUID id);
}
