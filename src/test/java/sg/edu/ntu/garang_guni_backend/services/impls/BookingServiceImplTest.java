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
import sg.edu.ntu.garang_guni_backend.entities.BookingRequest;
import sg.edu.ntu.garang_guni_backend.entities.CollectionType;
import sg.edu.ntu.garang_guni_backend.entities.Item;
import sg.edu.ntu.garang_guni_backend.entities.Location;
import sg.edu.ntu.garang_guni_backend.entities.PaymentMethod;
import sg.edu.ntu.garang_guni_backend.exceptions.booking.BookingNotFoundException;
import sg.edu.ntu.garang_guni_backend.repositories.BookingRepository;
import sg.edu.ntu.garang_guni_backend.services.ItemService;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingServiceImpl bookingService;
    private static BookingRequest sampleBookingRequest;
    private static Booking sampleBooking;
    private static Booking updatedBookingDetails;
    private static Booking sampleBookingWithItems;
    private static Item sampleItem;
    private static Location sampleLocation;
    private static final String SAMPLE_USER_ID = "1111-1111-1111-1111";
    private static final LocalDateTime SAMPLE_BOOKING_DATE_TIME = LocalDateTime.parse("2024-09-25T14:30:00");
    private static final LocalDateTime SAMPLE_APPOINTMENT_DATE_TIME = LocalDateTime.parse("2024-09-27T14:30:00");
    private static final String SAMPLE_REMARKS = "What is this Test?";
    private static final String UPDATE_REMARKS = "What is this updated Test?";
    private static final String SAMPLE_ITEM_NAME = "Aluminium Cans";
    private static final String SAMPLE_ITEM_DESCRIPTION = "It's a metal can.";
    private static final String SAMPLE_LOCATION_NAME = "Fitzroy";
    private static final String SAMPLE_LOCATION_ADDRESS = "104 Cecil Street";
    private static final BigDecimal SAMPLE_LOCATION_LAT = BigDecimal.valueOf(1.281285);
    private static final BigDecimal SAMPLE_LOCATION_LNG = BigDecimal.valueOf(103.848961);

    @BeforeAll
    static void setUp() {
        sampleBookingRequest = BookingRequest.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
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

        updatedBookingDetails = Booking.builder()
                .userId(SAMPLE_USER_ID)
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

        sampleBookingWithItems = Booking.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .items(List.of(sampleItem))
                .build();

        sampleLocation = Location.builder()
                .locationName(SAMPLE_LOCATION_NAME)
                .locationAddress(SAMPLE_LOCATION_ADDRESS)
                .latitude(SAMPLE_LOCATION_LAT)
                .longitude(SAMPLE_LOCATION_LNG)
                .build();
    }

    @DisplayName("Create Booking - Successful")
    @Test
    void createBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking savedBooking = invocation.getArgument(0);
                    savedBooking.setBookingId(bookingId);
                    return savedBooking;
                });

        Booking createdBooking = bookingService.createBooking(sampleBookingRequest);

        assertEquals(bookingId, createdBooking.getBookingId());
        assertEquals(SAMPLE_USER_ID, createdBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                createdBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                createdBooking.getAppointmentDateTime());
        assertEquals(true, createdBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, createdBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, createdBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, createdBooking.getRemarks());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @DisplayName("Get Booking By Id - Successful")
    @Test
    void getBookingByIdTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleBooking));

        Booking retrievedBooking = bookingService.getBookingById(bookingId);

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
        verify(bookingRepository, times(1)).findById(any(UUID.class));
    }

    @DisplayName("Get Booking By Id - Invalid Booking Id")
    @Test
    void getBookingByInvalidIdTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBookingById(bookingId));
    }

    @DisplayName("Update Booking - Successful")
    @Test
    void updateBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(new Booking(sampleBooking)));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking savedBooking = invocation.getArgument(0);
                    savedBooking.setBookingId(bookingId);
                    return savedBooking;
                });

        Booking updatedBooking = bookingService.updateBooking(bookingId, updatedBookingDetails);

        assertNotEquals(updatedBookingDetails, updatedBooking);
        assertEquals(SAMPLE_USER_ID, updatedBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                updatedBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                updatedBooking.getAppointmentDateTime());
        assertEquals(true, updatedBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, updatedBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, updatedBooking.getPaymentMethod());
        assertEquals(UPDATE_REMARKS, updatedBooking.getRemarks());
        verify(bookingRepository, times(1)).findById(any(UUID.class));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @DisplayName("Update Booking - Invalid Booking Id")
    @Test
    void updateBookingWithInvalidIdTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.updateBooking(bookingId, updatedBookingDetails));
    }

    @DisplayName("Delete Booking - Successful")
    @Test
    void deleteBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleBooking));

        Booking deletedBooking = bookingService.deleteBooking(bookingId);

        assertEquals(sampleBooking, deletedBooking);
        assertEquals(SAMPLE_USER_ID, deletedBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                deletedBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                deletedBooking.getAppointmentDateTime());
        assertEquals(true, deletedBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, deletedBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, deletedBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, deletedBooking.getRemarks());
        verify(bookingRepository, times(1)).findById(any(UUID.class));
        verify(bookingRepository, times(1)).deleteById(any(UUID.class));
    }

    @DisplayName("Delete Booking - Invalid Booking Id")
    @Test
    void deleteBookingWithInvalidIdTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.deleteBooking(bookingId));
    }

    @DisplayName("Add New Item To Booking - Successful")
    @Test
    void addNewItemToBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleBooking));
        when(itemService.assignBookingToNewItem(any(Item.class), any(Booking.class)))
                .thenReturn(new Item(sampleItem));

        Item addedItem = bookingService.addNewItemToBooking(bookingId, sampleItem);
        assertNotEquals(sampleItem, addedItem);
        assertEquals(SAMPLE_ITEM_NAME, addedItem.getItemName());
        assertEquals(SAMPLE_ITEM_DESCRIPTION, addedItem.getItemDescription());
        verify(bookingRepository, times(1)).findById(any(UUID.class));
    }

    @DisplayName("Add New Item To Booking - Invalid Booking Id")
    @Test
    void addNewItemToBookingWithInvalidIdTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.addNewItemToBooking(bookingId, sampleItem));
    }

    @DisplayName("Add Existing Item To Booking - Successful")
    @Test
    void addExistingItemToBookingTest() {
        UUID bookingId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleBooking));
        when(itemService.assignBookingToExistingItem(any(UUID.class), any(Booking.class)))
                .thenReturn(new Item(sampleItem));

        Item addedItem = bookingService.addExistingItemToBooking(bookingId, itemId);
        assertNotEquals(sampleItem, addedItem);
        assertEquals(SAMPLE_ITEM_NAME, addedItem.getItemName());
        assertEquals(SAMPLE_ITEM_DESCRIPTION, addedItem.getItemDescription());
        verify(bookingRepository, times(1)).findById(any(UUID.class));
    }

    @DisplayName("Add New Item To Booking - Invalid Booking Id")
    @Test
    void addExistingItemToBookingWithInvalidIdTest() {
        UUID bookingId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.addExistingItemToBooking(bookingId, itemId));
    }

    @DisplayName("Add All Items - Successful")
    @Test
    void getAllItemsTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleBookingWithItems));

        List<Item> items = bookingService.getAllItems(bookingId);
        assertEquals(1, items.size());
        Item retrievedItem = items.get(0);
        assertNotEquals(sampleItem, retrievedItem);
        assertEquals(SAMPLE_ITEM_NAME, retrievedItem.getItemName());
        assertEquals(SAMPLE_ITEM_DESCRIPTION, retrievedItem.getItemDescription());
        verify(bookingRepository, times(1)).findById(any(UUID.class));
    }

    @DisplayName("Add All Items - Invalid Booking Id")
    @Test
    void getAllItemsWithInvalidBookingIdTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getAllItems(bookingId));
    }

    @DisplayName("Assign Location To New Booking - Successful")
    @Test
    void assignLocationToNewBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking savedBooking = invocation.getArgument(0);
                    savedBooking.setBookingId(bookingId);
                    return savedBooking;
                });

        Booking assignedBooking = bookingService
                .assignLocationToNewBooking(sampleBooking, sampleLocation);

        assertEquals(sampleLocation, assignedBooking.getLocation());
        assertEquals(bookingId, assignedBooking.getBookingId());
        assertEquals(SAMPLE_USER_ID, assignedBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                assignedBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                assignedBooking.getAppointmentDateTime());
        assertEquals(true, assignedBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, assignedBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, assignedBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, assignedBooking.getRemarks());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @DisplayName("Assign Location To Existing Booking - Successful")
    @Test
    void assignLocationToExistingBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking savedBooking = invocation.getArgument(0);
                    savedBooking.setBookingId(bookingId);
                    return savedBooking;
                });

        Booking assignedBooking = bookingService
                .assignLocationToExistingBooking(bookingId, sampleLocation);

        assertEquals(sampleLocation, assignedBooking.getLocation());
        assertEquals(bookingId, assignedBooking.getBookingId());
        assertEquals(SAMPLE_USER_ID, assignedBooking.getUserId());
        assertEquals(SAMPLE_BOOKING_DATE_TIME,
                assignedBooking.getBookingDateTime());
        assertEquals(SAMPLE_APPOINTMENT_DATE_TIME,
                assignedBooking.getAppointmentDateTime());
        assertEquals(true, assignedBooking.isLocationSameAsRegistered());
        assertEquals(CollectionType.HOME, assignedBooking.getCollectionType());
        assertEquals(PaymentMethod.VISA, assignedBooking.getPaymentMethod());
        assertEquals(SAMPLE_REMARKS, assignedBooking.getRemarks());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @DisplayName("Assign Location To Existing Booking - Invalid Booking Id")
    @Test
    void assignLocationToNonExistingBookingTest() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService
                .assignLocationToExistingBooking(bookingId, sampleLocation));
    }
}
