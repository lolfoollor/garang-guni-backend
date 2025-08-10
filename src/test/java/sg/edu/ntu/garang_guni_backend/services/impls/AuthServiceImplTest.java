package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sg.edu.ntu.garang_guni_backend.configs.security.details.CustomUserDetails;
import sg.edu.ntu.garang_guni_backend.configs.security.services.JwtService;
import sg.edu.ntu.garang_guni_backend.dtos.AuthResponse;
import sg.edu.ntu.garang_guni_backend.dtos.LoginRequest;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.exceptions.UserExistsException;
import sg.edu.ntu.garang_guni_backend.mappers.AuthRequestUserMapper;
import sg.edu.ntu.garang_guni_backend.services.UserService;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceImplTest {
    private static final String VALID_USERNAME = "Test";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "P@ssword123";
    private static final String FAKE_JWT_TOKEN = "FakeJwtToken";
    private static final String FAKE_JWT_TOKEN_TYPE = "Bearer";

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthRequestUserMapper authRequestUserMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RegistrationRequest registrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .roles(Set.of(UserRole.USER))
                .isEnabled(true)
                .build();

        registrationRequest = RegistrationRequest.builder()
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();

        loginRequest = LoginRequest.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();
    }

    @DisplayName("Register with Valid User - Successful")
    @Test
    void registerSuccessTest() {
        when(authRequestUserMapper.toEntity(registrationRequest)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn(FAKE_JWT_TOKEN);

        AuthResponse response = authService.register(registrationRequest);

        assertEquals(FAKE_JWT_TOKEN, response.accessToken());
        assertEquals(FAKE_JWT_TOKEN_TYPE, response.tokenType());
        verify(authRequestUserMapper, times(1)).toEntity(registrationRequest);
        verify(userService, times(1)).createUser(user);
        verify(jwtService, times(1)).generateToken(any());
    }

    @DisplayName("Register with Existing User Email - Conflict")
    @Test
    void registerWithExistingEmailTest() {
        when(authRequestUserMapper.toEntity(registrationRequest)).thenReturn(user);
        when(userService.createUser(user)).thenThrow(new UserExistsException(user.getEmail()));

        assertThrows(UserExistsException.class,
                () -> authService.register(registrationRequest));
        verify(jwtService, never()).generateToken(any());
    }

    @DisplayName("Authenticate with Valid Credentials - Successful")
    @Test
    void authenticateSuccessTest() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(),
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_PASSWORD,
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + UserRole.USER.getRoleName())),
                true);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(any())).thenReturn(FAKE_JWT_TOKEN);

        AuthResponse response = authService.authenticate(loginRequest);

        assertEquals(FAKE_JWT_TOKEN, response.accessToken());
        assertEquals(FAKE_JWT_TOKEN_TYPE, response.tokenType());
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(any());
    }

    @DisplayName("Authenticate with Invalid/ Unregistered Credentials - BadCredentialsException")
    @Test
    void authenticatewithInvalidCredentialsTest() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(loginRequest));
        verify(jwtService, never()).generateToken(any());
    }
}
