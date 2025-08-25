package sg.edu.ntu.garang_guni_backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import sg.edu.ntu.garang_guni_backend.entities.Booking;
import sg.edu.ntu.garang_guni_backend.entities.BookingRequest;
import sg.edu.ntu.garang_guni_backend.entities.CollectionType;
import sg.edu.ntu.garang_guni_backend.entities.Location;
import sg.edu.ntu.garang_guni_backend.entities.PaymentMethod;
import sg.edu.ntu.garang_guni_backend.exceptions.booking.BookingNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.location.LocationNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "testuser", roles = { "USER" })
class LocationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private static Location sampleLocation;
    private static Location sampleLocationWithoutAddress;
    private static Location sampleLocationWithoutName;
    private static Location sampleLocationWithoutLat;
    private static Location sampleLocationWithoutLng;
    private static Location sampleLocationWithInvalidLat;
    private static Location sampleLocationWithInvalidLng;
    private static Location updatedLocation;
    private static Booking sampleBooking;
    private static Booking invalidBooking;
    private static BookingRequest sampleBookingRequest;
    private static final String SAMPLE_LOCATION_NAME = "Fitzroy";
    private static final String SAMPLE_LOCATION_ADDRESS = "104 Cecil Street";
    private static final String UPDATED_LOCATION_NAME = "My Home";
    private static final String UPDATED_LOCATION_ADDRESS = "100 Cecil Street";
    private static final BigDecimal SAMPLE_LOCATION_LAT = BigDecimal.valueOf(1.281285);
    private static final BigDecimal SAMPLE_LOCATION_LNG = BigDecimal.valueOf(103.848961);
    private static final BigDecimal INVALID_LOCATION_LAT = BigDecimal.valueOf(1.2811726568429875);
    private static final BigDecimal INVALID_LOCATION_LNG = BigDecimal.valueOf(103.84897437092968);
    private static final String SAMPLE_USER_ID = "1111-1111-1111-1111";
    private static final LocalDateTime SAMPLE_BOOKING_DATE_TIME = LocalDateTime
            .parse("2024-09-25T14:30:00");
    private static final LocalDateTime SAMPLE_APPOINTMENT_DATE_TIME = LocalDateTime
            .parse("2024-09-27T14:30:00");
    private static final String SAMPLE_BOOKING_DATE_TIME_PLAIN = "2024-09-25T14:30:00";
    private static final String SAMPLE_APPOINTMENT_DATE_TIME_PLAIN = "2024-09-27T14:30:00";
    private static final String SAMPLE_REMARKS = "What is this Test?";
    private static final String PROBLEM_DETAIL_CONTENT_TYPE = "application/problem+json";

    @BeforeAll
    void setUp() {
        sampleLocation = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();

        sampleLocationWithoutAddress = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();

        sampleLocationWithoutName = Location.builder()
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();
        sampleLocationWithoutLat = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();
        sampleLocationWithoutLng = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .build();
        sampleLocationWithInvalidLat = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(INVALID_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();
        sampleLocationWithInvalidLng = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(INVALID_LOCATION_LNG)
                .build();
        updatedLocation = Location.builder()
                .locationName(UPDATED_LOCATION_NAME)
                .locationAddress(UPDATED_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();

        sampleBooking = Booking.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .build();

        invalidBooking = Booking.builder()
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .build();

        sampleBookingRequest = BookingRequest.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .build();
    }

    @DisplayName("Create Location - Successful")
    @Test
    void createLocationTest() throws Exception {
        postVerifyAndRetrieveLocationResponse(sampleLocation);
        postVerifyAndRetrieveLocationResponse(sampleLocationWithoutAddress);
    }

    @DisplayName("Create Location - Missing Parameters")
    @Test
    void createLocationWithMissingParametersTest() throws Exception {
        postVerifyBadRequest(sampleLocationWithoutName);
        postVerifyBadRequest(sampleLocationWithoutLat);
        postVerifyBadRequest(sampleLocationWithoutLng);
        postVerifyBadRequest(sampleLocationWithInvalidLat);
        postVerifyBadRequest(sampleLocationWithInvalidLng);
    }

    @DisplayName("Get Location By Id - Successful")
    @Test
    void getLocationByIdTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/locations/" + createdlocationId);

        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.locationId").value(createdlocationId))
                .andExpect(jsonPath("$.locationName").value(SAMPLE_LOCATION_NAME))
                .andExpect(jsonPath("$.locationAddress").value(SAMPLE_LOCATION_ADDRESS))
                .andExpect(jsonPath("$.latitude").value(SAMPLE_LOCATION_LAT))
                .andExpect(jsonPath("$.longitude").value(SAMPLE_LOCATION_LNG));
    }

    @DisplayName("Get Location By Id - Invalid id")
    @Test
    void getLocationByNonExistantIdTest() throws Exception {
        String createdlocationId = UUID.randomUUID().toString();
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/locations/" + createdlocationId);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Get Locations Without booking - Successful")
    @Test
    void getLocationsTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdLocationAsJson2 = postVerifyAndRetrieveLocationResponse(sampleLocationWithoutAddress);

        RequestBuilder getAllLocationRequest = MockMvcRequestBuilders.get("/locations");

        String retrievedLocationJson = mockMvc.perform(getAllLocationRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Location> locations = objectMapper.readValue(retrievedLocationJson,
                new TypeReference<List<Location>>() {
                });

        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");
        String createdlocationId2 = JsonPath.read(createdLocationAsJson2, "$.locationId");

        // Verify its really 1 sample location and 1 sample without address
        List<Location> locationsLeft = locations.stream()
                .filter(location -> location.getLocationId().toString().equals(createdlocationId))
                .filter(location -> location.getLocationName().equals(SAMPLE_LOCATION_NAME))
                .filter(location -> location.getLocationAddress().equals(SAMPLE_LOCATION_ADDRESS))
                .filter(location -> location.getLatitude().equals(SAMPLE_LOCATION_LAT))
                .filter(location -> location.getLongitude().equals(SAMPLE_LOCATION_LNG))
                .filter(location -> location.getBookings() == null).toList();

        assertEquals(1, locationsLeft.size());

        locationsLeft = locations.stream()
                .filter(location -> location.getLocationId().toString().equals(createdlocationId2))
                .filter(location -> location.getLocationName().equals(SAMPLE_LOCATION_NAME))
                .filter(location -> location.getLocationAddress() == null)
                .filter(location -> location.getLatitude().equals(SAMPLE_LOCATION_LAT))
                .filter(location -> location.getLongitude().equals(SAMPLE_LOCATION_LNG))
                .filter(location -> location.getBookings() == null).toList();

        assertEquals(1, locationsLeft.size());
    }

    @DisplayName("Update Location - Successful")
    @Test
    void updateLocationTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        String updatedLocationAsJson = objectMapper.writeValueAsString(updatedLocation);

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/locations/" + createdlocationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedLocationAsJson);

        mockMvc.perform(putRequest).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.locationName").value(UPDATED_LOCATION_NAME))
                .andExpect(jsonPath("$.latitude").value(SAMPLE_LOCATION_LAT))
                .andExpect(jsonPath("$.longitude").value(SAMPLE_LOCATION_LNG))
                .andExpect(jsonPath("$.locationAddress").value(UPDATED_LOCATION_ADDRESS));
    }

    @DisplayName("Update Location - Invalid Id")
    @Test
    void updateLocationWithInvalidIdTest() throws Exception {
        String createdlocationId = UUID.randomUUID().toString();

        String updatedLocationAsJson = objectMapper.writeValueAsString(updatedLocation);

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/locations/" + createdlocationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedLocationAsJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Update Location - Invalid Location")
    @Test
    void updateLocationWithInvalidLocationsTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        putVerifyBadRequest(sampleLocationWithoutName, createdlocationId);
        putVerifyBadRequest(sampleLocationWithoutLat, createdlocationId);
        putVerifyBadRequest(sampleLocationWithoutLng, createdlocationId);
        putVerifyBadRequest(sampleLocationWithInvalidLat, createdlocationId);
        putVerifyBadRequest(sampleLocationWithInvalidLng, createdlocationId);
    }

    @DisplayName("Delete Location - Successful")
    @Test
    void deleteLocationTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath
                .read(createdLocationAsJson, "$.locationId");

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/locations/" + createdlocationId);

        mockMvc.perform(deleteRequest).andExpect(status().isNoContent());

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get("/locations/" + createdlocationId);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Delete Location - Invalid Id")
    @Test
    void deleteLocationWithInvalidIdTest() throws Exception {
        String createdlocationId = UUID.randomUUID().toString();

        RequestBuilder deleteRequest = MockMvcRequestBuilders
                .delete("/locations/" + createdlocationId);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Add New Booking To Location - Successful")
    @Test
    void addNewBookingToLocationTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        String sampleBookingAsJson = objectMapper.writeValueAsString(sampleBooking);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/locations/" + createdlocationId + "/bookings")
                .contentType(MediaType.APPLICATION_JSON).content(sampleBookingAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.location.locationId").value(createdlocationId))
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(SAMPLE_REMARKS));
    }

    @DisplayName("Add New Booking To Location - Invalid Location Id")
    @Test
    void addNewBookingToNonExistantLocationTest() throws Exception {
        String createdlocationId = UUID.randomUUID().toString();

        String sampleBookingAsJson = objectMapper.writeValueAsString(sampleBooking);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/locations/" + createdlocationId + "/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleBookingAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Add New Booking To Location - Missing Parameters")
    @Test
    void addInvalidBookingToLocationTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        String invalidBookingAsJson = objectMapper.writeValueAsString(invalidBooking);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/locations/" + createdlocationId + "/bookings")
                .contentType(MediaType.APPLICATION_JSON).content(invalidBookingAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @DisplayName("Add Exisiting Booking To Location - Successful")
    @Test
    void addExistingBookingToLocationTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        String sampleBookingAsJson = objectMapper.writeValueAsString(sampleBookingRequest);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleBookingAsJson);

        String createdBookingAsJson = mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(SAMPLE_REMARKS)).andReturn().getResponse()
                .getContentAsString();

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/locations/" + createdlocationId + "/bookings/" + createdBookingId);

        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.location.locationId").value(createdlocationId));
    }

    @DisplayName("Add Exisiting Booking To Location - Invalid Location Id")
    @Test
    void addExistingBookingToNonExistantLocationTest() throws Exception {
        String createdlocationId = UUID.randomUUID().toString();

        String sampleBookingAsJson = objectMapper.writeValueAsString(sampleBookingRequest);

        RequestBuilder postRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON).content(sampleBookingAsJson);

        String createdBookingAsJson = mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(SAMPLE_REMARKS)).andReturn().getResponse()
                .getContentAsString();

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/locations/" + createdlocationId + "/bookings/" + createdBookingId);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Add Exisiting Booking To Location - Invalid Booking Id")
    @Test
    void addNonExistantBookingToLocationTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        String createdBookingId = UUID.randomUUID().toString();

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/locations/" + createdlocationId + "/bookings/" + createdBookingId);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Get All Booking - Successful")
    @Test
    void getAllBookingsTest() throws Exception {
        String createdLocationAsJson = postVerifyAndRetrieveLocationResponse(sampleLocation);
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        String sampleBookingAsJson = objectMapper.writeValueAsString(sampleBooking);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/locations/" + createdlocationId + "/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleBookingAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.location.locationId").value(createdlocationId))
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(SAMPLE_REMARKS));

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get("/locations/" + createdlocationId + "/bookings");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].location.locationId").value(createdlocationId))
                .andExpect(jsonPath("$.[0].userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.[0].bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.[0].appointmentDateTime")
                        .value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.[0].locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.[0].collectionType").value("HOME"))
                .andExpect(jsonPath("$.[0].paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.[0].remarks").value(SAMPLE_REMARKS));
    }

    @DisplayName("Get All Booking - Invalid Location Id")
    @Test
    void getAllBookingsWithInvalidLocationIdTest() throws Exception {
        String createdlocationId = UUID.randomUUID().toString();

        String sampleBookingAsJson = objectMapper.writeValueAsString(sampleBookingRequest);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleBookingAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(SAMPLE_REMARKS));

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get("/locations/" + createdlocationId + "/bookings");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    private String postVerifyAndRetrieveLocationResponse(Location locationToCreate)
            throws Exception {
        String sampleLocationAsJson = objectMapper.writeValueAsString(locationToCreate);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleLocationAsJson);

        ResultActions result = mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        if (locationToCreate.getLocationAddress() != null) {
            result.andExpect(jsonPath("$.locationAddress").value(SAMPLE_LOCATION_ADDRESS));
        }

        return result.andExpect(jsonPath("$.locationId").exists())
                .andExpect(jsonPath("$.locationName").value(SAMPLE_LOCATION_NAME))
                .andExpect(jsonPath("$.latitude").value(SAMPLE_LOCATION_LAT))
                .andExpect(jsonPath("$.longitude").value(SAMPLE_LOCATION_LNG)).andReturn()
                .getResponse().getContentAsString();
    }

    private void postVerifyBadRequest(Location malformLocation) throws Exception {
        String sampleLocationAsJson = objectMapper.writeValueAsString(malformLocation);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleLocationAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    private void putVerifyBadRequest(Location malformLocation, String id) throws Exception {
        String newMalformLocationAsJson = objectMapper.writeValueAsString(malformLocation);

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/locations/" + id)
                .contentType(MediaType.APPLICATION_JSON).content(newMalformLocationAsJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MethodArgumentNotValidException));
    }
}
