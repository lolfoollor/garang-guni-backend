package sg.edu.ntu.garang_guni_backend.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.entities.Availability;
import sg.edu.ntu.garang_guni_backend.entities.AvailabilityRequest;
import sg.edu.ntu.garang_guni_backend.entities.Location;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvailabilityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Availability availability;
    private static AvailabilityRequest availabilityRequest;
    private static Location location;
    private static Location updatedLocation;
    private static final String LOCATION_NAME = "Test Location";
    private static final String UPDATED_LOCATION_NAME = "Updated Location";
    private static final LocalDate AVAILABLE_DATE = LocalDate.now().plusDays(10);

    @BeforeAll
    static void setUp() {
        location = new Location();
        location.setLocationName(LOCATION_NAME);
        location.setLatitude(BigDecimal.valueOf(1.281285));
        location.setLongitude(BigDecimal.valueOf(1.281285));

        availability = new Availability();
        availability.setAvailableDate(AVAILABLE_DATE);
        availability.setLocation(location);

        availabilityRequest = AvailabilityRequest.builder()
                .availableDate(AVAILABLE_DATE)
                .location(location).build();

        updatedLocation = new Location();
        updatedLocation.setLocationName(UPDATED_LOCATION_NAME);
        updatedLocation.setLatitude(BigDecimal.valueOf(1.3521));
        updatedLocation.setLongitude(BigDecimal.valueOf(103.8198));
    }

    @Test
    @DisplayName("Test Creating Availability")
    @WithMockUser(username = "scrapdealer", roles = { "BUYER, USER" })
    void testCreatingAvailability() throws Exception {
        String sampleAvailabilityRequestAsJson = objectMapper
                .writeValueAsString(availabilityRequest);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleAvailabilityRequestAsJson);

        mockMvc.perform(postRequest).andExpect(status().isCreated())
                .andExpect(jsonPath("$.location.locationName").value(LOCATION_NAME));
    }
}
