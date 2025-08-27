package sg.edu.ntu.garang_guni_backend.integrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.configs.security.services.JwtService;
import sg.edu.ntu.garang_guni_backend.dtos.AuthResponse;
import sg.edu.ntu.garang_guni_backend.dtos.JwtUserDetails;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.exceptions.ErrorType;
import sg.edu.ntu.garang_guni_backend.mappers.AuthRequestUserMapper;
import sg.edu.ntu.garang_guni_backend.repositories.UserRepository;
import sg.edu.ntu.garang_guni_backend.services.AuthService;
import sg.edu.ntu.garang_guni_backend.services.OtpService;
import sg.edu.ntu.garang_guni_backend.services.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmailVerificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private AuthRequestUserMapper authRequestUserMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private User user;
    private static final String VALID_USERNAME = "Test";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "ValidPassword123!";
    private static final String VALID_OTP = "VALID12345";
    private static final String FAKE_JWT_TOKEN = "FakeJwtToken";
    private static final String FAKE_JWT_TOKEN_TYPE = "Bearer";
    private static final String VERIFY_EMAIL_URL = "/api/verification/email/verify?"
            + "email=%s&otp=%s".formatted(VALID_EMAIL, VALID_OTP);
    private static final String RESEND_VERIFY_EMAIL_URL = "/api/verification/email"
            + "/resend-verification?email=%s".formatted(VALID_EMAIL);
    private static final String PROBLEM_DETAIL_CONTENT_TYPE = "application/problem+json";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .email(VALID_EMAIL)
                .isEnabled(true)
                .isEmailVerified(false)
                .roles(Set.of(UserRole.USER))
                .build();
    }

    @DisplayName("Register Sends Verification OTP - Sucessful")
    @Test
    void registerSendsVerificationOtpTest() {
        RegistrationRequest registerRequest = RegistrationRequest.builder()
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();

        when(authRequestUserMapper.toEntity(registerRequest)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(user);
        when(jwtService.generateToken(any(JwtUserDetails.class))).thenReturn(FAKE_JWT_TOKEN);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(FAKE_JWT_TOKEN, response.accessToken());
        assertEquals(FAKE_JWT_TOKEN_TYPE, response.tokenType());

        verify(userService).createUser(user);
        verify(jwtService).generateToken(any(JwtUserDetails.class));

        verify(otpService, times(1)).generateAndStoreOtp(user.getId());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @DisplayName("Resend verification OTP - Successful")
    @Test
    void resendVerificationOtpTest() throws Exception {
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(user));

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post(RESEND_VERIFY_EMAIL_URL)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
        verify(otpService, times(1)).generateAndStoreOtp(user.getId());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @DisplayName("Resend verification OTP - User Not Found")
    @Test
    void resendVerificationOtpUserNotFoundTest() throws Exception {
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post(RESEND_VERIFY_EMAIL_URL)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(jsonPath("$.type").value(
                        ErrorType.NOT_FOUND.getUri().toString()));

        verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
        verify(otpService, never()).generateAndStoreOtp(any());
    }

    @DisplayName("Resend verification OTP - Already Verified")
    @Test
    void resendVerificationOtpAlreadyVerifiedTest() throws Exception {
        user.setEmailVerified(true);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post(RESEND_VERIFY_EMAIL_URL)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(jsonPath("$.type").value(
                        ErrorType.EMAIL_ALREADY_VERIFIED.getUri().toString()));

        verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
        verify(otpService, never()).generateAndStoreOtp(any());
    }

    @DisplayName("Verify email - Sucessful")
    @Test
    void verifyEmailTest() throws Exception {
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(user));
        when(otpService.isOtpValid(user.getId(), VALID_OTP))
                .thenReturn(true);

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.username").value(VALID_USERNAME));

        verify(otpService, times(1)).isOtpValid(user.getId(), VALID_OTP);
        verify(otpService, times(1)).deleteOtp(user.getId());
    }

    @DisplayName("Verify email - Invalid User")
    @Test
    void verifyEmailInvalidUserTest() throws Exception {
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.empty());

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(jsonPath("$.type").value(
                        ErrorType.NOT_FOUND.getUri().toString()));

        verify(otpService, never()).isOtpValid(user.getId(), VALID_OTP);
        verify(otpService, never()).deleteOtp(any());
    }

    @DisplayName("Verify email - Already Verified")
    @Test
    void verifyAlreadyValidEmailTest() throws Exception {
        user.setEmailVerified(true);
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(user));

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(jsonPath("$.type").value(
                        ErrorType.EMAIL_ALREADY_VERIFIED.getUri().toString()));

        verify(otpService, never()).isOtpValid(user.getId(), VALID_OTP);
        verify(otpService, never()).deleteOtp(any());
    }

    @DisplayName("Verify email - Invalid OTP")
    @Test
    void verifyEmailInvalidOtpTest() throws Exception {
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(user));
        when(otpService.isOtpValid(user.getId(), VALID_OTP))
                .thenReturn(false);

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().is4xxClientError());

        verify(otpService, times(1)).isOtpValid(user.getId(), VALID_OTP);
        verify(otpService, never()).deleteOtp(any());
    }
}
