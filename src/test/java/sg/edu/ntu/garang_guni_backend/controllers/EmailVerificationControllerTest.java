package sg.edu.ntu.garang_guni_backend.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import jakarta.transaction.Transactional;
import sg.edu.ntu.garang_guni_backend.dtos.UserProfileResponse;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.exceptions.UserNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.EmailAlreadyVerifiedException;
import sg.edu.ntu.garang_guni_backend.exceptions.email.InvalidTokenException;
import sg.edu.ntu.garang_guni_backend.mappers.UserMapper;
import sg.edu.ntu.garang_guni_backend.services.EmailVerificationService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class EmailVerificationControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String VALID_USERNAME = "Test";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "P@ssword123";
    private static final String OTP = "ABCDE12345";
    private static final String VERIFY_EMAIL_URL = "/api/verification/email/verify?"
            + "email=%s&otp=%s".formatted(VALID_EMAIL, OTP);
    private static final String RESEND_VERIFY_EMAIL_URL = "/api/verification/email"
            + "/resend-verification?email=%s".formatted(VALID_EMAIL);

    private static final String PROBLEM_DETAIL_CONTENT_TYPE = "application/problem+json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private EmailVerificationService emailVerificationService;
    private static User verifiedUser;
    private static UserProfileResponse userProfileResponse;

    @BeforeAll
    static void init() {
        verifiedUser = User.builder()
                .id(ID)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .roles(Set.of(UserRole.USER))
                .isEnabled(true)
                .isEmailVerified(true)
                .build();

        userProfileResponse = new UserProfileResponse(
                ID,
                VALID_EMAIL,
                VALID_USERNAME,
                List.of(UserRole.USER.getRoleName()));

    }

    @DisplayName("Verify Email - Successful")
    @Test
    void verifyUser() throws Exception {
        when(emailVerificationService.verifyEmail(VALID_EMAIL, OTP)).thenReturn(verifiedUser);
        when(userMapper.toUserProfileDto(verifiedUser)).thenReturn(userProfileResponse);

        RequestBuilder getRequest = MockMvcRequestBuilders.get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.username").value(VALID_USERNAME));

        verify(emailVerificationService, times(1)).verifyEmail(VALID_EMAIL, OTP);
        verify(userMapper, times(1)).toUserProfileDto(verifiedUser);
    }

    @DisplayName("Resend Verification Email - Successful")
    @Test
    void resendVerificationEmail() throws Exception {
        doNothing().when(emailVerificationService).resendVerificationOtp(VALID_EMAIL);
        RequestBuilder postRequest = MockMvcRequestBuilders.post(RESEND_VERIFY_EMAIL_URL);

        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());

        verify(emailVerificationService, times(1)).resendVerificationOtp(VALID_EMAIL);
    }

    @DisplayName("Verify Email - Unregistered Email")
    @Test
    void verifyUserWithUnregisteredEmail() throws Exception {
        when(emailVerificationService.verifyEmail(VALID_EMAIL, OTP))
                .thenThrow(new UserNotFoundException(VALID_EMAIL));

        RequestBuilder getRequest = MockMvcRequestBuilders.get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof UserNotFoundException));
    }

    @DisplayName("Verify Email - Already Verified")
    @Test
    void verifyVerifiedUser() throws Exception {
        when(emailVerificationService.verifyEmail(VALID_EMAIL, OTP))
                .thenThrow(new EmailAlreadyVerifiedException(VALID_EMAIL));

        RequestBuilder getRequest = MockMvcRequestBuilders.get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof EmailAlreadyVerifiedException));
    }

    @DisplayName("Verify Email - Invalid OTP")
    @Test
    void verifyUserWithInvalidOtp() throws Exception {
        when(emailVerificationService.verifyEmail(VALID_EMAIL, OTP))
                .thenThrow(new InvalidTokenException("Token invalid or expired"));

        RequestBuilder getRequest = MockMvcRequestBuilders.get(VERIFY_EMAIL_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof InvalidTokenException));
    }

    @DisplayName("Resend Verification Email - Unregistered Email")
    @Test
    void resendVerificationToUnregisteredEmail() throws Exception {
        doThrow(new UserNotFoundException(VALID_EMAIL))
                .when(emailVerificationService).resendVerificationOtp(VALID_EMAIL);

        RequestBuilder postRequest = MockMvcRequestBuilders.post(RESEND_VERIFY_EMAIL_URL);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof UserNotFoundException));
    }

    @DisplayName("Resend Verification Email - Already Verified")
    @Test
    void resendVerificationToAlreadyVerifiedEmail() throws Exception {
        doThrow(new EmailAlreadyVerifiedException(VALID_EMAIL))
                .when(emailVerificationService).resendVerificationOtp(VALID_EMAIL);

        RequestBuilder postRequest = MockMvcRequestBuilders.post(RESEND_VERIFY_EMAIL_URL);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof EmailAlreadyVerifiedException));
    }
}
