package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.ntu.garang_guni_backend.entities.Booking;
import sg.edu.ntu.garang_guni_backend.entities.CollectionType;
import sg.edu.ntu.garang_guni_backend.entities.Location;
import sg.edu.ntu.garang_guni_backend.entities.PaymentMethod;
import sg.edu.ntu.garang_guni_backend.exceptions.location.LocationNotFoundException;
import sg.edu.ntu.garang_guni_backend.repositories.LocationRepository;
import sg.edu.ntu.garang_guni_backend.services.BookingService;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {
    @Mock
    private LocationRepository locationRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private LocationServiceImpl locationService;
    private static Location sampleLocation;
    private static Location updatedLocation;
    private static Location sampleLocationWithBookings;
    private static Booking sampleBooking;
    private static final String SAMPLE_LOCATION_NAME = "Fitzroy";
    private static final String SAMPLE_LOCATION_ADDRESS = "104 Cecil Street";
    private static final String UPDATED_LOCATION_NAME = "My Home";
    private static final String UPDATED_LOCATION_ADDRESS = "100 Cecil Street";
    private static final BigDecimal SAMPLE_LOCATION_LAT = BigDecimal.valueOf(1.281285);
    private static final BigDecimal SAMPLE_LOCATION_LNG = BigDecimal.valueOf(103.848961);
    private static final UUID LOCATION_ID = UUID.randomUUID();
    private static final String SAMPLE_USER_ID = "1111-1111-1111-1111";
    private static final LocalDateTime SAMPLE_BOOKING_DATE_TIME = LocalDateTime.parse("2024-09-25T14:30:00");
    private static final LocalDateTime SAMPLE_APPOINTMENT_DATE_TIME = LocalDateTime.parse("2024-09-27T14:30:00");
    private static final String SAMPLE_REMARKS = "What is this Test?";

    @BeforeAll
    static void setUp() {
        sampleLocation = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
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

        sampleLocationWithBookings = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .bookings(List.of(sampleBooking))
                .build();
    }

    @DisplayName("Create Location - Successful")
    @Test
    void createLocationTest() {
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });
        Location createdLocation = locationService.createLocation(sampleLocation);
        assertEquals(sampleLocation.getLocationName(),
                createdLocation.getLocationName());
        assertEquals(sampleLocation.getLocationAddress(),
                createdLocation.getLocationAddress());
        assertEquals(sampleLocation.getLatitude(),
                createdLocation.getLatitude());
        assertEquals(sampleLocation.getLongitude(),
                createdLocation.getLongitude());
        verify(locationRepository, times(1))
                .save(any(Location.class));
    }

    @DisplayName("Get Location By Id - Successful")
    @Test
    void getLocationByIdTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.of(sampleLocation));

        Location retrievedLocation = locationService.getLocationById(LOCATION_ID);
        assertNotEquals(sampleLocation, retrievedLocation);
        assertEquals(sampleLocation.getLocationName(),
                retrievedLocation.getLocationName());
        assertEquals(sampleLocation.getLocationAddress(),
                retrievedLocation.getLocationAddress());
        assertEquals(sampleLocation.getLatitude(),
                retrievedLocation.getLatitude());
        assertEquals(sampleLocation.getLongitude(),
                retrievedLocation.getLongitude());
        verify(locationRepository, times(1))
                .findById(any(UUID.class));
    }

    @DisplayName("Get Location By Id - Invalid Id")
    @Test
    void getLocationByNonExistantIdTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationService.getLocationById(LOCATION_ID));
    }

    @DisplayName("Update Location - Successful")
    @Test
    void updateLocationTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.of(sampleLocation));
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        Location retrievedLocation = locationService
                .updateLocation(LOCATION_ID, updatedLocation);

        assertNotEquals(updatedLocation, retrievedLocation);
        assertEquals(updatedLocation.getLocationName(),
                retrievedLocation.getLocationName());
        assertEquals(updatedLocation.getLocationAddress(),
                retrievedLocation.getLocationAddress());
        assertEquals(updatedLocation.getLatitude(),
                retrievedLocation.getLatitude());
        assertEquals(updatedLocation.getLongitude(),
                retrievedLocation.getLongitude());
        verify(locationRepository, times(1))
                .findById(any(UUID.class));
        verify(locationRepository, times(1))
                .save(any(Location.class));
    }

    @DisplayName("Update Location - Invalid Id")
    @Test
    void updateLocationWithNonExistentIdTest() {
        assertThrows(LocationNotFoundException.class,
                () -> locationService.updateLocation(LOCATION_ID, updatedLocation));
    }

    @DisplayName("Delete Location - Successful")
    @Test
    void deleteLocationTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.of(sampleLocation));

        Location popLocation = locationService.deleteLocation(LOCATION_ID);
        assertEquals(sampleLocation, popLocation);
        verify(locationRepository, times(1))
                .findById(LOCATION_ID);
        verify(locationRepository, times(1))
                .deleteById(LOCATION_ID);
    }

    @DisplayName("Delete Location - Invalid Id")
    @Test
    void deleteLocationWithNonExistentIdTest() {
        assertThrows(LocationNotFoundException.class,
                () -> locationService.deleteLocation(LOCATION_ID));
    }

    @DisplayName("Add New Booking To Location - Successful")
    @Test
    void addNewBookingToLocationTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.of(sampleLocation));
        when(bookingService.assignLocationToNewBooking(
                any(Booking.class), any(Location.class)))
                .thenReturn(new Booking(sampleBooking));

        Booking newBooking = locationService.addNewBookingToLocation(LOCATION_ID, sampleBooking);

        assertNotEquals(sampleBooking, newBooking);
        assertEquals(SAMPLE_USER_ID, newBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                newBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                newBooking.getAppointmentDateTime());
        assertEquals(true, newBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, newBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, newBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, newBooking.getRemarks());
        verify(locationRepository, times(1)).findById(LOCATION_ID);
    }

    @DisplayName("Add New Booking To Location - Invalid Location Id")
    @Test
    void addNewBookingToInvalidLocationTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationService
                        .addNewBookingToLocation(LOCATION_ID, sampleBooking));
    }

    @DisplayName("Add Exisiting Booking To Location - Successful")
    @Test
    void addExistingBookingToLocationTest() {
        UUID bookingId = UUID.randomUUID();
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.of(sampleLocation));
        when(bookingService.assignLocationToExistingBooking(
                any(UUID.class), any(Location.class)))
                .thenReturn(new Booking(sampleBooking));

        Booking newBooking = locationService.addExisitingBookingToLocation(LOCATION_ID, bookingId);

        assertNotEquals(sampleBooking, newBooking);
        assertEquals(SAMPLE_USER_ID, newBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                newBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                newBooking.getAppointmentDateTime());
        assertEquals(true, newBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, newBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, newBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, newBooking.getRemarks());
        verify(locationRepository, times(1)).findById(LOCATION_ID);
    }

    @DisplayName("Add Existing Booking To Location - Invalid Location Id")
    @Test
    void addExistingBookingToInvalidLocationTest() {
        UUID bookingId = UUID.randomUUID();
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationService
                        .addExisitingBookingToLocation(LOCATION_ID, bookingId));
    }

    @DisplayName("Get All Bookings - Successful")
    @Test
    void getAllBookingsTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.of(sampleLocationWithBookings));

        List<Booking> bookings = locationService.getAllBookings(LOCATION_ID);
        assertEquals(1, bookings.size());
        Booking retrievedBooking = bookings.get(0);
        assertNotEquals(sampleBooking, retrievedBooking);
        assertEquals(SAMPLE_USER_ID, retrievedBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                retrievedBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                retrievedBooking.getAppointmentDateTime());
        assertEquals(true, retrievedBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, retrievedBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, retrievedBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, retrievedBooking.getRemarks());
        verify(locationRepository, times(1)).findById(LOCATION_ID);
    }

    @DisplayName("Get All Bookings - Invalid Location Id")
    @Test
    void getAllBookingsWithInvalidLocationIdTest() {
        when(locationRepository.findById(LOCATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationService.getAllBookings(LOCATION_ID));
    }
}
