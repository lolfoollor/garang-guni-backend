package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.exceptions.UserNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.EmailAlreadyVerifiedException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.InvalidTokenException;
import sg.edu.ntu.garang_guni_backend.repositories.UserRepository;
import sg.edu.ntu.garang_guni_backend.services.OtpService;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceImplTest {

    @Mock
    private OtpService otpService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final String VALID_OTP = "ABCDE12345";
    private static final String INCORRECT_OTP = "WRONGOTP12";
    private static final String VALID_USERNAME = "Test";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "P@ssword123";
    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(VALID_ID)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .roles(Set.of(UserRole.USER))
                .isEnabled(true)
                .isEmailVerified(false)
                .build();
    }

    @DisplayName("Send Verification OTP - Successful")
    @Test
    void sendVerificationOtpTest() {
        when(otpService.generateAndStoreOtp(VALID_ID)).thenReturn(VALID_OTP);

        emailVerificationService.sendVerificationOtp(VALID_ID, VALID_EMAIL);

        verify(otpService, times(1)).generateAndStoreOtp(VALID_ID);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @DisplayName("Resend Verification OTP - Successful")
    @Test
    void resendVerificationOtpTest() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpService.generateAndStoreOtp(VALID_ID)).thenReturn(VALID_OTP);

        emailVerificationService.resendVerificationOtp(user.getEmail());

        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(otpService, times(1)).generateAndStoreOtp(VALID_ID);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @DisplayName("Resend Verification OTP - Invalid Email")
    @Test
    void resendVerificationOtpUserNotFoundTest() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> emailVerificationService.resendVerificationOtp(user.getEmail()));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Resend Verification OTP - Already Verified")
    void resendVerificationOtpAlreadyVerifiedTest() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyVerifiedException.class,
                () -> emailVerificationService.resendVerificationOtp(user.getEmail()));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Verify Email - Successful")
    void verifyEmailTest() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpService.isOtpValid(user.getId(), VALID_OTP)).thenReturn(true);

        User resultUser = emailVerificationService.verifyEmail(user.getEmail(), VALID_OTP);

        assertTrue(resultUser.isEmailVerified());
        verify(otpService, times(1)).deleteOtp(user.getId());
    }

    @DisplayName("Verify Email - Invalid Email")
    @Test
    void verifyEmailUserNotFoundTest() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> emailVerificationService.verifyEmail(user.getEmail(), VALID_OTP));
    }

    @Test
    @DisplayName("Verify Email - Already Verified")
    void verifyEmailAlreadyVerifiedTest() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyVerifiedException.class,
                () -> emailVerificationService.resendVerificationOtp(user.getEmail()));

        verify(otpService, never()).deleteOtp(user.getId());
    }

    @Test
    @DisplayName("Verify Email - Invalid OTP")
    void verifyEmailInvalidOtpTest() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpService.isOtpValid(user.getId(), INCORRECT_OTP)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> emailVerificationService.verifyEmail(user.getEmail(), INCORRECT_OTP));

        verify(otpService, never()).deleteOtp(user.getId());
    }
}
