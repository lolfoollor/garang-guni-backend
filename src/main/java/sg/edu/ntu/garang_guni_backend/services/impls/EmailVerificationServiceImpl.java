package sg.edu.ntu.garang_guni_backend.services.impls;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.exceptions.UserNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.EmailAlreadyVerifiedException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.InvalidTokenException;
import sg.edu.ntu.garang_guni_backend.repositories.UserRepository;
import sg.edu.ntu.garang_guni_backend.services.EmailVerificationService;
import sg.edu.ntu.garang_guni_backend.services.OtpService;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private OtpService otpService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private UserRepository userRepository;

    private JavaMailSender mailSender;

    public EmailVerificationServiceImpl(
            OtpService otpService,
            UserRepository userRepository,
            JavaMailSender mailSender) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendVerificationOtp(UUID userId, String email) {
        String otp = otpService.generateAndStoreOtp(userId);
        String emailText = "Please enter the following email verification " +
                "code: " + otp + ".";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setFrom("System");
        message.setText(emailText);

        mailSender.send(message);
    }

    @Override
    public void resendVerificationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }

        sendVerificationOtp(user.getId(), user.getEmail());
    }

    @Override
    @Transactional
    public User verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }

        if (!otpService.isOtpValid(user.getId(), otp)) {
            throw new InvalidTokenException("Token invalid or expired");
        }

        otpService.deleteOtp(user.getId());

        user.setEmailVerified(true);

        return user;
    }

}
