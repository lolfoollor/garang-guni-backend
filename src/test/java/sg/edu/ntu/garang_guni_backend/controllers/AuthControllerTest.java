package sg.edu.ntu.garang_guni_backend.controllers;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import sg.edu.ntu.garang_guni_backend.dtos.LoginRequest;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String REGISTRATION_URL = "/auth/register";
    private static final String LOGIN_URL = "/auth/login";
    private static final String VALID_USERNAME = "Test";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "P@ssword123";
    private static final String INVALID_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static RegistrationRequest validRegistrationRequest;
    private static RegistrationRequest invalidRegistrationRequest;

    @BeforeAll
    static void setUpUsers() {
        validRegistrationRequest = RegistrationRequest
                .builder()
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();

        invalidRegistrationRequest = RegistrationRequest
                .builder()
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(INVALID_PASSWORD)
                .build();
    }

    private RequestBuilder registrationRequest(
            RegistrationRequest registrationRequest) throws Exception {
        String userJson = objectMapper.writeValueAsString(registrationRequest);

        return MockMvcRequestBuilders.post(REGISTRATION_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson);
    }

    private RequestBuilder loginRequest(
            String email, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        return MockMvcRequestBuilders.post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson);
    }

    @DisplayName("Register with Valid User - Successful")
    @Test
    void registerSuccessTest() throws Exception {
        RequestBuilder registrationRequest = registrationRequest(validRegistrationRequest);

        mockMvc.perform(registrationRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(notNullValue()));
    }

    @DisplayName("Register with Invalid User - Bad Request")
    @Test
    void registerInvalidTest() throws Exception {
        RequestBuilder registrationRequest = registrationRequest(invalidRegistrationRequest);

        mockMvc.perform(registrationRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(notNullValue()));
    }

    @DisplayName("Register with Exisiting User Email - Conflict")
    @Test
    void registerExistingEmailTest() throws Exception {
        RequestBuilder registrationRequest = registrationRequest(validRegistrationRequest);

        mockMvc.perform(registrationRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(notNullValue()));

        mockMvc.perform(registrationRequest)
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(notNullValue()));
    }

    @DisplayName("Login with Valid Credientials - Successful")
    @Test
    void authenticateSuccessTest() throws Exception {
        RequestBuilder registerRequest = registrationRequest(validRegistrationRequest);

        mockMvc.perform(registerRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(notNullValue()));

        RequestBuilder loginRequest = loginRequest(VALID_EMAIL, VALID_PASSWORD);

        mockMvc.perform(loginRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(notNullValue()));
    }

    @DisplayName("Login with Invalid Credientials - Unauthorized")
    @Test
    void authenticateInvalidCredentialsTest() throws Exception {
        RequestBuilder registerRequest = registrationRequest(validRegistrationRequest);

        mockMvc.perform(registerRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(notNullValue()));

        RequestBuilder loginRequest = loginRequest(VALID_EMAIL, "Wr0ngP@ssword");

        mockMvc.perform(loginRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @DisplayName("Login with Unregistered Credentials - Unauthorized")
    @Test
    void authenticateNonExistentUserTest() throws Exception {
        RequestBuilder loginRequest = loginRequest(VALID_EMAIL, VALID_PASSWORD);

        mockMvc.perform(loginRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
