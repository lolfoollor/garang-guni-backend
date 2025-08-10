package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.ntu.garang_guni_backend.entities.Availability;
import sg.edu.ntu.garang_guni_backend.entities.Location;
import sg.edu.ntu.garang_guni_backend.entities.ScrapDealer;
import sg.edu.ntu.garang_guni_backend.repositories.AvailabilityRepository;
import sg.edu.ntu.garang_guni_backend.repositories.ScrapDealerRepository;
import sg.edu.ntu.garang_guni_backend.services.LocationService;

class AvailabilityServiceImplTest {

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Mock
    private LocationService locationService;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private ScrapDealerRepository scrapDealerRepository;

    private Availability availability;
    private ScrapDealer scrapDealer;
    private Location mockLocation;
    private static final LocalDate AVAILABLE_DATE = LocalDate.now().plusDays(10);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        scrapDealer = new ScrapDealer();
        scrapDealer.setScrapDealerId(UUID.randomUUID());

        mockLocation = new Location();
        mockLocation.setLocationName("Test Location");
        mockLocation.setLatitude(BigDecimal.valueOf(1.281285));
        mockLocation.setLongitude(BigDecimal.valueOf(1.281285));

        availability = new Availability();
        availability.setAvailableDate(AVAILABLE_DATE);
        availability.setLocation(mockLocation);
    }

    @Test
    @DisplayName("Test Updating Availability Date Successfully")
    void testUpdateAvailabilityDate() {
        when(availabilityRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(availability));
        when(availabilityRepository.save(any(Availability.class)))
                .thenReturn(availability);

        Availability updatedAvailability = availabilityService
                .updateAvailability(UUID.randomUUID(), availability);

        assertEquals(AVAILABLE_DATE, updatedAvailability.getAvailableDate());
    }

    @Test
    @DisplayName("Test Deleting Availability")
    void testDeleteAvailability() {
        when(availabilityRepository.existsById(any(UUID.class)))
                .thenReturn(true);

        availabilityService.deleteAvailability(UUID.randomUUID());
        verify(availabilityRepository, times(1))
                .deleteById(any(UUID.class));
    }

}
