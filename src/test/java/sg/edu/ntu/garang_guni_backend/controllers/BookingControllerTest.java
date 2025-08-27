package sg.edu.ntu.garang_guni_backend.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;
import sg.edu.ntu.garang_guni_backend.entities.Booking;
import sg.edu.ntu.garang_guni_backend.entities.BookingRequest;
import sg.edu.ntu.garang_guni_backend.entities.CollectionType;
import sg.edu.ntu.garang_guni_backend.entities.Item;
import sg.edu.ntu.garang_guni_backend.entities.Location;
import sg.edu.ntu.garang_guni_backend.entities.PaymentMethod;
import sg.edu.ntu.garang_guni_backend.exceptions.booking.BookingNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.item.ItemNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.location.LocationNotFoundException;
import sg.edu.ntu.garang_guni_backend.utils.ImageUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "testuser", roles = { "USER" })
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private static BookingRequest sampleBookingRequest;
    private static BookingRequest sampleInvalidBookingRequest;
    private static Booking updatedBooking;
    private static Booking invalidBooking;
    private static Location sampleLocation;
    private static MockMultipartFile fakeImgFile;
    private static MockMultipartFile fakeImgFile2;
    private static Item sampleItem;
    private static Item sampleItem2;
    private static Item invalidItem;
    private static final String SAMPLE_USER_ID = "1111-1111-1111-1111";
    private static final LocalDateTime SAMPLE_BOOKING_DATE_TIME = LocalDateTime
            .parse("2024-09-25T14:30:00");
    private static final LocalDateTime SAMPLE_APPOINTMENT_DATE_TIME = LocalDateTime
            .parse("2024-09-27T14:30:00");
    private static final String SAMPLE_BOOKING_DATE_TIME_PLAIN = "2024-09-25T14:30:00";
    private static final String SAMPLE_APPOINTMENT_DATE_TIME_PLAIN = "2024-09-27T14:30:00";
    private static final String SAMPLE_REMARKS = "What is this Test?";
    private static final String UPDATE_REMARKS = "What is this updated Test?";
    private static final String SAMPLE_LOCATION_NAME = "Fitzroy";
    private static final String SAMPLE_LOCATION_ADDRESS = "104 Cecil Street";
    private static final BigDecimal SAMPLE_LOCATION_LAT = BigDecimal.valueOf(1.281285);
    private static final BigDecimal SAMPLE_LOCATION_LNG = BigDecimal.valueOf(103.848961);
    private static final String SAMPLE_ITEM_NAME = "Aluminium Cans";
    private static final String SAMPLE_ITEM_DESCRIPTION = "It's a metal can.";
    private static final String SAMPLE_ITEM_2_NAME = "Steel Cans";
    private static final String SAMPLE_ITEM_2_DESCRIPTION = "It's STILL a metal can.";
    private static final String SAMPLE_FILE_NAME = "test_booking_image.png";
    private static final String SAMPLE_FILE_NAME2 = "test_booking_image2.png";
    private static final String SAMPLE_IMAGE_TYPE = "image/png";
    private static final byte[] SAMPLE_FILE_DATA = "This is a test image".getBytes();
    private static final byte[] SAMPLE_FILE_DATA2 = "This is a test image2".getBytes();
    private static final String PROBLEM_DETAIL_CONTENT_TYPE = "application/problem+json";

    @BeforeAll
    void setUp() {
        sampleBookingRequest = BookingRequest.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME).isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME).paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS).build();

        sampleLocation = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS).latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG).build();

        fakeImgFile = new MockMultipartFile(
                "items[0].images",
                SAMPLE_FILE_NAME,
                SAMPLE_IMAGE_TYPE,
                SAMPLE_FILE_DATA);

        fakeImgFile2 = new MockMultipartFile(
                "items[1].images",
                SAMPLE_FILE_NAME2,
                SAMPLE_IMAGE_TYPE,
                SAMPLE_FILE_DATA2);

        sampleInvalidBookingRequest = BookingRequest.builder()
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA).remarks(SAMPLE_REMARKS)
                .build();

        updatedBooking = Booking.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(UPDATE_REMARKS)
                .build();

        invalidBooking = Booking.builder()
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(UPDATE_REMARKS)
                .build();

        sampleItem = Item.builder()
                .itemName(SAMPLE_ITEM_NAME)
                .itemDescription(SAMPLE_ITEM_DESCRIPTION)
                .build();

        sampleItem2 = Item.builder()
                .itemName(SAMPLE_ITEM_2_NAME)
                .itemDescription(SAMPLE_ITEM_2_DESCRIPTION)
                .build();

        invalidItem = Item.builder()
                .itemDescription(SAMPLE_ITEM_2_DESCRIPTION)
                .build();
    }

    @DisplayName("Create Booking - Successful")
    @Test
    void createBookingTest() throws Exception {
        postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);
        postVerifyAndRetrieveSampleBookingWithItemResponse(null);

        String createdLocationAsJson = postVerifyAndRetrieveSampleLocationResponse();
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        BookingRequest sampleBookingRequestWithLocation = BookingRequest.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .locationId(UUID.fromString(createdlocationId))
                .build();

        postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequestWithLocation);
        postVerifyAndRetrieveSampleBookingWithItemResponse(createdlocationId);
    }

    @DisplayName("Create Booking - Invalid parameters")
    @Test
    void createBookingWithMissingParametersTest() throws Exception {
        postVerifyBadRequest(sampleInvalidBookingRequest);
        String createdLocationAsJson = postVerifyAndRetrieveSampleLocationResponse();
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        BookingRequest sampleInvalidBookingRequestWithLocation = BookingRequest.builder()
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .locationId(UUID.fromString(createdlocationId))
                .build();

        postVerifyBadRequest(sampleInvalidBookingRequestWithLocation);
    }

    @DisplayName("Create Booking - Invalid Location Id")
    @Test
    void createBookingWithInvalidLocationIdTest() throws Exception {
        UUID createdlocationId = UUID.randomUUID();

        BookingRequest sampleInvalidBookingRequestWithLocation = BookingRequest.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .locationId(createdlocationId)
                .build();

        String invalidBookingRequestAsJson = objectMapper
                .writeValueAsString(sampleInvalidBookingRequestWithLocation);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBookingRequestAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof LocationNotFoundException));
    }

    @DisplayName("Get Booking By Id - Successful")
    @Test
    void getBookingByIdTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        getAndVerifySampleBooking(createdBookingId, null);

        String createdLocationAsJson = postVerifyAndRetrieveSampleLocationResponse();
        String createdlocationId = JsonPath.read(createdLocationAsJson, "$.locationId");

        BookingRequest sampleBookingRequestWithLocation = BookingRequest.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .locationId(UUID.fromString(createdlocationId))
                .build();

        String createdBookingWithLocationAsJson = postVerifyAndRetrieveSampleBookingResponse(
                sampleBookingRequestWithLocation);

        String createdBookingWithLocationId = JsonPath
                .read(createdBookingWithLocationAsJson, "$.bookingId");

        getAndVerifySampleBooking(createdBookingWithLocationId, createdlocationId);

        String createdBookingWithItemAsJson = postVerifyAndRetrieveSampleBookingWithItemResponse(null);

        String createdBookingWithItemId = JsonPath
                .read(createdBookingWithItemAsJson, "$.bookingId");

        getAndVerifySampleBooking(createdBookingWithItemId, null);

        String createdBookingWithItemAndLocationAsJson = postVerifyAndRetrieveSampleBookingWithItemResponse(
                createdlocationId);

        String createdBookingWithItemAndLocationId = JsonPath
                .read(createdBookingWithItemAndLocationAsJson, "$.bookingId");

        getAndVerifySampleBooking(createdBookingWithItemAndLocationId, createdlocationId);
    }

    @DisplayName("Get Booking By Id - Invalid Booking Id")
    @Test
    void getBookingByNonExistantIdTest() throws Exception {
        String createdBookingId = UUID.randomUUID().toString();

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/bookings/" + createdBookingId);

        mockMvc.perform(getRequest).andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Update Booking By Id - Successful")
    @Test
    void updateBookingByIdTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String sampleUpdatedAsJson = objectMapper.writeValueAsString(updatedBooking);

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/bookings/" + createdBookingId)
                .contentType(MediaType.APPLICATION_JSON).content(sampleUpdatedAsJson);

        mockMvc.perform(putRequest)
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(UPDATE_REMARKS));
    }

    @DisplayName("Update Booking By Id - Invalid Id")
    @Test
    void updateBookingByInvalidIdTest() throws Exception {

        String createdBookingId = UUID.randomUUID().toString();

        String sampleUpdatedAsJson = objectMapper.writeValueAsString(updatedBooking);

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/bookings/" + createdBookingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleUpdatedAsJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Update Booking By Id - Invalid Booking")
    @Test
    void updateBookingByIdWithInvalidParametersTest() throws Exception {

        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String sampleUpdatedAsJson = objectMapper.writeValueAsString(invalidBooking);

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/bookings/" + createdBookingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleUpdatedAsJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @DisplayName("Delete Booking By Id - Successful")
    @Test
    void deleteBookingByIdTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        RequestBuilder deleteRequest = MockMvcRequestBuilders
                .delete("/bookings/" + createdBookingId);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/bookings/" + createdBookingId);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Delete Booking By Id - Invalid Booking Id")
    @Test
    void deleteBookingWithInvalidIdTest() throws Exception {
        String createdBookingId = UUID.randomUUID().toString();

        RequestBuilder deleteRequest = MockMvcRequestBuilders
                .delete("/bookings/" + createdBookingId);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Add New Item To Booking - Successful")
    @Test
    void addNewItemToBookingTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String uriPath = "/bookings/" + createdBookingId + "/items";
        String createdItemAsJson = postVerifyAndRetrieveSampleItemResponse(sampleItem, uriPath);

        String createdItemId = JsonPath.read(createdItemAsJson, "$.itemId");

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/items/" + createdItemId);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.booking.bookingId").value(createdBookingId));
    }

    @DisplayName("Add New Item To Booking - Invalid Booking Id")
    @Test
    void addNewItemToNonExistantBookingTest() throws Exception {
        String createdBookingId = UUID.randomUUID().toString();

        String sampleItemAsJson = objectMapper.writeValueAsString(sampleItem);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/bookings/" + createdBookingId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleItemAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Add New Item To Booking - Invalid Item")
    @Test
    void addNewItemToBookingWithMissingParamTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String invalidItemAsJson = objectMapper.writeValueAsString(invalidItem);

        RequestBuilder postRequest = MockMvcRequestBuilders
                .post("/bookings/" + createdBookingId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidItemAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @DisplayName("Add Exisiting Item To Booking - Successful")
    @Test
    void addExistingItemToBookingTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String uriPath = "/items";
        String createdItemAsJson = postVerifyAndRetrieveSampleItemResponse(sampleItem2, uriPath);

        String createdItemId = JsonPath.read(createdItemAsJson, "$.itemId");

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/bookings/" + createdBookingId + "/items/" + createdItemId);

        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.booking.bookingId").value(createdBookingId));
    }

    @DisplayName("Add exisiting Item To Booking - Invalid Booking Id")
    @Test
    void addExistingItemToNonExistantBookingTest() throws Exception {
        String createdBookingId = UUID.randomUUID().toString();

        String uriPath = "/items";
        String createdItemAsJson = postVerifyAndRetrieveSampleItemResponse(sampleItem2, uriPath);

        String createdItemId = JsonPath.read(createdItemAsJson, "$.itemId");

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/bookings/" + createdBookingId + "/items/" + createdItemId);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Add exisiting Item To Booking - Invalid Item Id")
    @Test
    void addNonExistantItemToBookingTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String createdItemId = UUID.randomUUID().toString();

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/bookings/" + createdBookingId + "/items/" + createdItemId);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof ItemNotFoundException));
    }

    @DisplayName("Add exisiting Item To Booking - Invalid Both Id")
    @Test
    void addNonExistantItemToNonExistantBookingTest() throws Exception {
        String createdBookingId = UUID.randomUUID().toString();

        String createdItemId = UUID.randomUUID().toString();

        RequestBuilder putRequest = MockMvcRequestBuilders
                .put("/bookings/" + createdBookingId + "/items/" + createdItemId);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    @DisplayName("Get All Items - Successful")
    @Test
    void getAllItemsTest() throws Exception {
        String createdBookingAsJson = postVerifyAndRetrieveSampleBookingResponse(sampleBookingRequest);

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        String uriPath = "/bookings/" + createdBookingId + "/items";
        String createdItemAsJson = postVerifyAndRetrieveSampleItemResponse(sampleItem, uriPath);

        String createdItemId = JsonPath.read(createdItemAsJson, "$.itemId");

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/items/" + createdItemId);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.booking.bookingId").value(createdBookingId));

        String createdItemAsJson2 = postVerifyAndRetrieveSampleItemResponse(sampleItem2, uriPath);

        String createdItemId2 = JsonPath.read(createdItemAsJson2, "$.itemId");

        RequestBuilder getRequest2 = MockMvcRequestBuilders.get("/items/" + createdItemId2);

        mockMvc.perform(getRequest2)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.booking.bookingId").value(createdBookingId));

        RequestBuilder getAllItemRequest = MockMvcRequestBuilders
                .get("/bookings/" + createdBookingId + "/items");

        mockMvc.perform(getAllItemRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].itemName").value(SAMPLE_ITEM_NAME))
                .andExpect(jsonPath("$.[0].itemDescription").value(SAMPLE_ITEM_DESCRIPTION))
                .andExpect(jsonPath("$.[1].itemName").value(SAMPLE_ITEM_2_NAME))
                .andExpect(jsonPath("$.[1].itemDescription").value(SAMPLE_ITEM_2_DESCRIPTION));
    }

    @DisplayName("Get All Items - Invalid Booking Id")
    @Test
    void getAllItemsWithNonExistantBookingTest() throws Exception {
        String createdBookingId = UUID.randomUUID().toString();

        String uriPath = "/items";
        postVerifyAndRetrieveSampleItemResponse(sampleItem, uriPath);
        postVerifyAndRetrieveSampleItemResponse(sampleItem2, uriPath);

        RequestBuilder getAllItemRequest = MockMvcRequestBuilders
                .get("/bookings/" + createdBookingId + "/items");

        mockMvc.perform(getAllItemRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof BookingNotFoundException));
    }

    private String postVerifyAndRetrieveSampleLocationResponse() throws Exception {
        String sampleLocationAsJson = objectMapper.writeValueAsString(sampleLocation);

        RequestBuilder postRequest = MockMvcRequestBuilders.post("/locations")
                .contentType(MediaType.APPLICATION_JSON).content(sampleLocationAsJson);

        return mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.locationId").exists())
                .andExpect(jsonPath("$.locationName").value(SAMPLE_LOCATION_NAME))
                .andExpect(jsonPath("$.locationAddress").value(SAMPLE_LOCATION_ADDRESS))
                .andExpect(jsonPath("$.latitude").value(SAMPLE_LOCATION_LAT))
                .andExpect(jsonPath("$.longitude").value(SAMPLE_LOCATION_LNG)).andReturn()
                .getResponse().getContentAsString();
    }

    private String postVerifyAndRetrieveSampleBookingResponse(BookingRequest bookingRequest)
            throws Exception {
        String sampleBookingRequestAsJson = objectMapper.writeValueAsString(bookingRequest);

        RequestBuilder postRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON).content(sampleBookingRequestAsJson);

        ResultActions result = mockMvc.perform(postRequest).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        UUID locationId = bookingRequest.getLocationId();
        if (locationId != null) {
            verifyLocationResult(result, locationId.toString());
        }

        ResultActions validatedResult = verifySampleBookingResult(result);

        return validatedResult.andReturn().getResponse().getContentAsString();
    }

    private void postVerifyBadRequest(BookingRequest bookingRequest) throws Exception {
        String sampleBookingRequestAsJson = objectMapper.writeValueAsString(bookingRequest);

        RequestBuilder postRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON).content(sampleBookingRequestAsJson);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    private String postVerifyAndRetrieveSampleBookingWithItemResponse(String locationId)
            throws Exception {
        boolean hasLocationId = locationId != null;

        RequestBuilder postRequest = MockMvcRequestBuilders.multipart("/bookings").file(fakeImgFile)
                .file(fakeImgFile2).param("userId", SAMPLE_USER_ID)
                .param("bookingDateTime", SAMPLE_BOOKING_DATE_TIME_PLAIN)
                .param("appointmentDateTime", SAMPLE_APPOINTMENT_DATE_TIME_PLAIN)
                .param("locationSameAsRegistered", "true").param("collectionType", "HOME")
                .param("paymentMethod", "VISA").param("remarks", SAMPLE_REMARKS)
                .param("locationId", hasLocationId ? locationId : null)
                .param("items[0].itemName", SAMPLE_ITEM_NAME)
                .param("items[0].itemDescription", SAMPLE_ITEM_DESCRIPTION)
                .param("items[1].itemName", SAMPLE_ITEM_2_NAME)
                .param("items[1].itemDescription", SAMPLE_ITEM_2_DESCRIPTION);

        ResultActions result = mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        if (hasLocationId) {
            verifyLocationResult(result, locationId);
        }

        ResultActions validatedResult = verifySampleBookingResult(result);

        String createdBookingAsJson = validatedResult
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdBookingId = JsonPath.read(createdBookingAsJson, "$.bookingId");

        RequestBuilder getItemsRequest = MockMvcRequestBuilders
                .get("/bookings/" + createdBookingId + "/items");

        String getItemsAsJson = mockMvc.perform(getItemsRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.[0].itemName").value(SAMPLE_ITEM_NAME))
                .andExpect(jsonPath("$.[0].itemDescription").value(SAMPLE_ITEM_DESCRIPTION))
                .andExpect(jsonPath("$.[1].itemName").value(SAMPLE_ITEM_2_NAME))
                .andExpect(jsonPath("$.[1].itemDescription").value(SAMPLE_ITEM_2_DESCRIPTION))
                .andReturn().getResponse().getContentAsString();

        String itemId = JsonPath.read(getItemsAsJson, "$.[0].itemId");
        String itemId2 = JsonPath.read(getItemsAsJson, "$.[1].itemId");

        getAndVerifyImageFile(itemId, fakeImgFile);
        getAndVerifyImageFile(itemId2, fakeImgFile2);

        return createdBookingAsJson;
    }

    private ResultActions verifyLocationResult(ResultActions currentBookingResult,
            String locationId) throws Exception {
        return currentBookingResult
                .andExpect(jsonPath("$.location").exists())
                .andExpect(jsonPath("$.location.locationId").value(locationId))
                .andExpect(jsonPath("$.location.locationName").value(SAMPLE_LOCATION_NAME))
                .andExpect(jsonPath("$.location.locationAddress").value(SAMPLE_LOCATION_ADDRESS))
                .andExpect(jsonPath("$.location.latitude").value(SAMPLE_LOCATION_LAT))
                .andExpect(jsonPath("$.location.longitude").value(SAMPLE_LOCATION_LNG));
    }

    private ResultActions verifySampleBookingResult(ResultActions currentBookingResult)
            throws Exception {
        return currentBookingResult
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.userId").value(SAMPLE_USER_ID))
                .andExpect(jsonPath("$.bookingDateTime").value(SAMPLE_BOOKING_DATE_TIME_PLAIN))
                .andExpect(
                        jsonPath("$.appointmentDateTime").value(SAMPLE_APPOINTMENT_DATE_TIME_PLAIN))
                .andExpect(jsonPath("$.locationSameAsRegistered").value(true))
                .andExpect(jsonPath("$.collectionType").value("HOME"))
                .andExpect(jsonPath("$.paymentMethod").value("VISA"))
                .andExpect(jsonPath("$.remarks").value(SAMPLE_REMARKS));
    }

    private void getAndVerifyImageFile(String itemId, MultipartFile file) throws Exception {
        RequestBuilder getImagesRequest = MockMvcRequestBuilders
                .get("/items/" + itemId + "/images/details");

        String compressedBase64FileData = ImageUtils
                .convertBytesArrToBase64(ImageUtils.compressImage(file.getBytes()));

        mockMvc.perform(getImagesRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].imageName").value(file.getOriginalFilename()))
                .andExpect(jsonPath("$.[0].imageType").value(file.getContentType()))
                .andExpect(jsonPath("$.[0].imageData").value(compressedBase64FileData));
    }

    private void getAndVerifySampleBooking(String bookingId, String locationId) throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/bookings/" + bookingId);

        ResultActions result = mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verifySampleBookingResult(result);
        if (locationId != null) {
            verifyLocationResult(result, locationId);
        }
    }

    private String postVerifyAndRetrieveSampleItemResponse(Item item, String uriPath)
            throws Exception {
        String itemAsJson = objectMapper.writeValueAsString(item);

        RequestBuilder postRequest = MockMvcRequestBuilders.post(uriPath)
                .contentType(MediaType.APPLICATION_JSON).content(itemAsJson);

        return mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemId").exists())
                .andExpect(jsonPath("$.itemName").value(item.getItemName()))
                .andExpect(jsonPath("$.itemDescription").value(item.getItemDescription()))
                .andReturn().getResponse().getContentAsString();
    }
}
