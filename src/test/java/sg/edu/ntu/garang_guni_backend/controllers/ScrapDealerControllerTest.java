package sg.edu.ntu.garang_guni_backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
import sg.edu.ntu.garang_guni_backend.entities.ScrapDealer;
import sg.edu.ntu.garang_guni_backend.exceptions.ErrorType;
import sg.edu.ntu.garang_guni_backend.services.ScrapDealerService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ScrapDealerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScrapDealerService scrapDealerService;

    @Autowired
    private ObjectMapper objectMapper;

    private ScrapDealer scrapDealer;
    private UUID loggedInUserId;

    @BeforeEach
    void setUp() {
        loggedInUserId = UUID.randomUUID();
        scrapDealer = new ScrapDealer();
        scrapDealer.setScrapDealerId(loggedInUserId);
        scrapDealer.setFirstName("Uncle");
        scrapDealer.setLastName("Roger");
        scrapDealer.setEmail("uncle@gmail.com");
        scrapDealer.setPhoneNumber("+6591234567");
    }

    @Test
    @DisplayName("Test creating a valid ScrapDealer")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer() throws Exception {
        when(scrapDealerService.createDealer(any(ScrapDealer.class))).thenReturn(scrapDealer);

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Uncle"))
                .andExpect(jsonPath("$.lastName").value("Roger"))
                .andExpect(jsonPath("$.email").value("uncle@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+6591234567"));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with missing first name")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_MissingFirstName() throws Exception {
        scrapDealer.setFirstName("");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with long first name")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_LongFirstName() throws Exception {
        scrapDealer.setFirstName("ThisFirstNameIsWayTooLongAndInvalid");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with missing last name")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_MissingLastName() throws Exception {
        scrapDealer.setLastName("");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with long last name")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_LongLastName() throws Exception {
        scrapDealer.setLastName("ThisLastNameIsWayTooLongAndInvalid");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with invalid email")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_InvalidEmail() throws Exception {
        scrapDealer.setEmail("invalid-email");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with empty email")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_EmptyEmail() throws Exception {
        scrapDealer.setEmail("");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test creating ScrapDealer with missing phone number")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_MissingPhoneNumber() throws Exception {
        scrapDealer.setPhoneNumber("");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists());
    }

    @Test
    @DisplayName("Test creating ScrapDealer with invalid phone number")
    @WithMockUser(username = "uncle@gmail.com", roles = { "SCRAP_DEALER" })
    void testCreateScrapDealer_InvalidPhoneNumber() throws Exception {
        scrapDealer.setPhoneNumber("123456");

        mockMvc.perform(post("/scrapdealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scrapDealer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        ErrorType.REQUEST_VALIDATION_FAILED.getUri().toString()));
    }

    @Test
    @DisplayName("Test retrieving all ScrapDealers")
    @WithMockUser(username = "admin@gmail.com", roles = { "SCRAP_DEALER" })
    void testGetAllScrapDealers() throws Exception {
        List<ScrapDealer> dealers = new ArrayList<>();
        dealers.add(scrapDealer);

        when(scrapDealerService.getAllDealers()).thenReturn(dealers);

        mockMvc.perform(get("/scrapdealers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Uncle"))
                .andExpect(jsonPath("$[0].lastName").value("Roger"))
                .andExpect(jsonPath("$[0].email").value("uncle@gmail.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("+6591234567"));
    }

    @Test
    @DisplayName("Test retrieving ScrapDealer by ID")
    @WithMockUser(username = "admin@gmail.com", roles = { "SCRAP_DEALER" })
    void testGetScrapDealerById() throws Exception {
        UUID dealerId = UUID.randomUUID();
        when(scrapDealerService.getScrapDealerById(dealerId)).thenReturn(scrapDealer);

        mockMvc.perform(get("/scrapdealers/" + dealerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Uncle"))
                .andExpect(jsonPath("$.lastName").value("Roger"))
                .andExpect(jsonPath("$.email").value("uncle@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+6591234567"));
    }

    @Test
    @DisplayName("Test deleting ScrapDealer by ID")
    @WithMockUser(username = "fe5d9416-8c91-49ec-93c5-1e83d7c5d913", roles = { "SCRAP_DEALER" })
    void testDeleteScrapDealerById() throws Exception {
        UUID dealerId = UUID.randomUUID();

        mockMvc.perform(delete("/scrapdealers/" + dealerId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test retrieving empty ScrapDealer list")
    @WithMockUser(username = "sd@gmail.com", roles = { "SCRAP_DEALER" })
    void testGetAllScrapDealers_EmptyList() throws Exception {
        when(scrapDealerService.getAllDealers()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/scrapdealers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}