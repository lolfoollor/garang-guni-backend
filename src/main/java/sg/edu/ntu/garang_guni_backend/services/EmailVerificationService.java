package sg.edu.ntu.garang_guni_backend.services;

import java.util.UUID;
import sg.edu.ntu.garang_guni_backend.entities.User;

public interface EmailVerificationService {
    void sendVerificationOtp(UUID userId, String email);

    void resendVerificationOtp(String email);

    User verifyEmail(String email, String otp);
}
