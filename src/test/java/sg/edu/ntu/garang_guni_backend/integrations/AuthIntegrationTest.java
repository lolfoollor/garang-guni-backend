package sg.edu.ntu.garang_guni_backend.integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;
    private String jwtAccessToken;
    private String uniqueEmail;
    private static final String TOKEN_PREFIX = "Bearer ";

    @BeforeAll
    void setUp() throws Exception {
        uniqueEmail = "test" + UUID.randomUUID().toString() + "@test.com";
        RegistrationRequest request = RegistrationRequest.builder().username("TestUsername")
                .email(uniqueEmail).password("SuperSekretPassword1!").build();

        String newRegisterationAsJson = objectMapper.writeValueAsString(request);

        RequestBuilder registrationRequest = MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(newRegisterationAsJson);

        String jsonResponse = mockMvc.perform(registrationRequest).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        jwtAccessToken = JsonPath.read(jsonResponse, "$.accessToken");
    }

    @DisplayName("Verify Valid Token")
    @Test
    void verifyValidTokenTest() {
        Jwt decodedJwt = jwtDecoder.decode(jwtAccessToken);

        assertThat(decodedJwt.getSubject()).isEqualTo(uniqueEmail);
    }

    @DisplayName("Verify Tempered Token")
    @Test
    void verifyInvalidTokenTest() {
        String tampered = jwtAccessToken.substring(0, jwtAccessToken.length() - 2) + "aa";

        assertThrows(BadJwtException.class, () -> {
            jwtDecoder.decode(tampered);
        });
    }

    @DisplayName("Get Secured Resource With Valid Token - Successful")
    @Test
    void getResourceFromSecuredEndpointWithValidToken() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.get("/users/me")
                .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + jwtAccessToken);

        mockMvc.perform(request).andExpect(status().isOk());
    }

    @DisplayName("Get Secured Resource With Invalid Token - Unauthorized")
    @Test
    void getResourceFromSecuredEndpointWithInvalidToken() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.get("/users/me")
                .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + "invalid token value");

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @DisplayName("Get Secured Resource With No Token - Unauthorized")
    @Test
    void getResourceFromSecuredEndpointWithNoToken() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.get("/users/me");

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }
}
