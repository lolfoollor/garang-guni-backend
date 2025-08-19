package sg.edu.ntu.garang_guni_backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.dtos.UserProfileResponse;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.mappers.UserMapper;
import sg.edu.ntu.garang_guni_backend.services.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com", roles = { "USER" })
class UserControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String VALID_USERNAME = "Test";
    private static final String UPDATED_USERNAME = "UpdatedTest";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "P@ssword123";
    private static final String GET_MY_PROFILE_URL = "/users/me";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static User user;
    private static User updatedUser;
    private static UserProfileResponse userProfileResponse;

    @BeforeAll
    static void init() {
        user = User.builder()
                .id(ID)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .roles(Set.of(UserRole.USER))
                .isEnabled(true)
                .build();

        updatedUser = User.builder()
                .id(ID)
                .username(UPDATED_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .roles(Set.of(UserRole.USER))
                .isEnabled(true)
                .build();

        userProfileResponse = new UserProfileResponse(
                ID,
                VALID_EMAIL,
                VALID_USERNAME,
                List.of(UserRole.USER.getRoleName()));

    }

    @DisplayName("Get Personal User Profile - Successful")
    @Test
    void getPersonalUserProfile() throws Exception {
        when(userService.getUserByEmail(VALID_EMAIL)).thenReturn(user);
        when(userMapper.toUserProfileDto(user)).thenReturn(userProfileResponse);

        RequestBuilder getRequest = MockMvcRequestBuilders.get(GET_MY_PROFILE_URL);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(VALID_EMAIL));

        verify(userService).getUserByEmail(VALID_EMAIL);
        verify(userMapper).toUserProfileDto(user);
    }

    @DisplayName("Get User By ID - Successful")
    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(ID)).thenReturn(user);

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get("/users/{id}", ID);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(VALID_EMAIL));

        verify(userService).getUserById(ID);
    }

    @DisplayName("Update User By ID - Successful")
    @Test
    void updateUserById() throws Exception {
        when(userService.updateUser(eq(ID), any(User.class)))
                .thenReturn(updatedUser);

        String updatedUserAsJson = objectMapper
                .writeValueAsString(updatedUser);

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/users/{id}", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserAsJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(UPDATED_USERNAME));

        verify(userService).updateUser(eq(ID), any(User.class));
    }
}
