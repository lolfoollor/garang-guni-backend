package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sg.edu.ntu.garang_guni_backend.entities.Booking;
import sg.edu.ntu.garang_guni_backend.entities.CollectionType;
import sg.edu.ntu.garang_guni_backend.entities.Image;
import sg.edu.ntu.garang_guni_backend.entities.Item;
import sg.edu.ntu.garang_guni_backend.entities.PaymentMethod;
import sg.edu.ntu.garang_guni_backend.exceptions.item.ItemNotFoundException;
import sg.edu.ntu.garang_guni_backend.repositories.ItemRepository;
import sg.edu.ntu.garang_guni_backend.services.ImageService;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ImageService imgService;

    @InjectMocks
    private ItemServiceImpl itemService;
    private static Booking sampleBooking;
    private static Item sampleItem;
    private static Item updatedItem;
    private static UUID itemId;
    private static MultipartFile imgFile;
    private static Image steelCanImage;
    private static Image steelCanSideImage;
    private static final String BASE_64_STEEL_CAN_IMG = "VGhpcyBpcyBhIFN0ZWVsIGNhbiBpbWFnZQ==";
    private static final String SAMPLE_USER_ID = "1111-1111-1111-1111";
    private static final LocalDateTime SAMPLE_BOOKING_DATE_TIME = LocalDateTime.parse("2024-09-25T14:30:00");
    private static final LocalDateTime SAMPLE_APPOINTMENT_DATE_TIME = LocalDateTime.parse("2024-09-27T14:30:00");
    private static final String SAMPLE_REMARKS = "What is this Test?";
    private static final String SAMPLE_ITEM_NAME = "Aluminium Cans";
    private static final String SAMPLE_ITEM_DESCRIPTION = "It's a metal can.";

    @BeforeAll
    static void setup() {
        itemId = UUID.randomUUID();

        sampleItem = Item.builder()
                .itemName(SAMPLE_ITEM_NAME)
                .itemDescription(SAMPLE_ITEM_DESCRIPTION)
                .build();

        steelCanImage = Image.builder()
                .imageName("Steel Can Photo")
                .imageType("image/png")
                .imageData("This is a Steel can image".getBytes())
                .build();

        steelCanSideImage = Image.builder()
                .imageName("Steel Can Side Photo")
                .imageType("image/png")
                .imageData("This is a Steel can side image".getBytes())
                .build();

        updatedItem = Item.builder()
                .itemName("Steel Cans")
                .itemDescription("It's STILL a metal can.")
                .images(List.of(steelCanImage, steelCanSideImage))
                .build();

        imgFile = new MockMultipartFile("image",
                "test_image.png",
                "image/png",
                "This is a test image".getBytes());

        sampleBooking = Booking.builder()
                .userId(SAMPLE_USER_ID)
                .bookingDateTime(SAMPLE_BOOKING_DATE_TIME)
                .appointmentDateTime(SAMPLE_APPOINTMENT_DATE_TIME)
                .isLocationSameAsRegistered(true)
                .collectionType(CollectionType.HOME)
                .paymentMethod(PaymentMethod.VISA)
                .remarks(SAMPLE_REMARKS)
                .build();
    }

    @DisplayName("Create Item - Successful")
    @Test
    void createItemTest() {
        when(itemRepository.save(any(Item.class))).thenReturn(sampleItem);

        Item createdItem = itemService.createItem(sampleItem);
        assertNotEquals(sampleItem, createdItem,
                "The saved Item should NOT be the same as the original Item!");
        assertEquals(sampleItem.getItemName(), createdItem.getItemName(),
                "The name of saved Item should be the same as original!");
        assertEquals(sampleItem.getItemDescription(), createdItem.getItemDescription(),
                "The description of saved Item should be the same as original!");
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @DisplayName("Assign Booking To New Item - Successful")
    @Test
    void assignBookingToNewItemTest() {
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item savedItem = invocation.getArgument(0);
                    savedItem.setItemId(itemId);
                    return savedItem;
                });

        Item assignedItem = itemService.assignBookingToNewItem(sampleItem, sampleBooking);

        assertEquals(itemId, assignedItem.getItemId());
        assertEquals(SAMPLE_ITEM_NAME, assignedItem.getItemName());
        assertEquals(SAMPLE_ITEM_DESCRIPTION, assignedItem.getItemDescription());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @DisplayName("Assign Booking To Existing Item - Successful")
    @Test
    void assignBookingToExistingItemTest() {
        when(itemRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item savedItem = invocation.getArgument(0);
                    savedItem.setItemId(itemId);
                    return savedItem;
                });

        Item assignedItem = itemService.assignBookingToExistingItem(itemId, sampleBooking);

        assertEquals(itemId, assignedItem.getItemId());
        assertEquals(SAMPLE_ITEM_NAME, assignedItem.getItemName());
        assertEquals(SAMPLE_ITEM_DESCRIPTION, assignedItem.getItemDescription());
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemRepository, times(1)).findById(any(UUID.class));
    }

    @DisplayName("Assign Booking To Existing Item - Invalid Item Id")
    @Test
    void assignBookingToNonExistingItemTest() {
        when(itemRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class,
                () -> itemService.assignBookingToExistingItem(itemId, sampleBooking));
    }

    @DisplayName("Get Item By Id - Successful")
    @Test
    void getItemByIdTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(sampleItem));

        Item retrievedItem = itemService.getItemById(itemId);
        assertEquals(sampleItem.getItemName(), retrievedItem.getItemName(),
                "The name of retrieved Item should be the same as original!");
        assertEquals(sampleItem.getItemDescription(), retrievedItem.getItemDescription(),
                "The description of retrieved Item should be the same as original!");
        verify(itemRepository, times(1)).findById(any(UUID.class));
    }

    @DisplayName("Get Item By Id - Invalid Id")
    @Test
    void getItemByIdWithNonExistentIdTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> itemService.getItemById(itemId));
    }

    @DisplayName("Update Item - Successful")
    @Test
    void updateItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        Item retrievedUpdatedItem = itemService.updateItem(itemId, updatedItem);

        assertNotEquals(updatedItem, retrievedUpdatedItem,
                "The saved Item should NOT be the same as the updated Item!");
        assertEquals(updatedItem.getItemName(), retrievedUpdatedItem.getItemName(),
                "The name of saved Item should be the same as the updated Item!");
        assertEquals(updatedItem.getItemDescription(), retrievedUpdatedItem.getItemDescription(),
                "The description of saved Item should be the same as the updated Item!");
        assertEquals(updatedItem.getImages().get(0).getImageName(),
                retrievedUpdatedItem.getImages().get(0).getImageName(),
                "The name of saved Item should be same as updated Item!");
        assertEquals(updatedItem.getImages().get(0).getImageType(),
                retrievedUpdatedItem.getImages().get(0).getImageType(),
                "The type of saved Item should be same as updated Item!");
        assertArrayEquals(updatedItem.getImages().get(0).getImageData(),
                retrievedUpdatedItem.getImages().get(0).getImageData(),
                "The data of saved Item should be same as updated Item!");
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @DisplayName("Update Item - Invalid Id")
    @Test
    void updateItemWithNonExistentId() {
        assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(itemId, updatedItem));
    }

    @DisplayName("Delete Item - Successful")
    @Test
    void deleteItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(sampleItem));

        Item popItem = itemService.deleteItem(itemId);
        assertEquals(popItem, sampleItem);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).deleteById(itemId);
    }

    @DisplayName("Delete Item - Invalid Id")
    @Test
    void deleteItemWithNonExistentId() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> itemService.deleteItem(itemId));
    }

    @DisplayName("Add New Image to Item - Successful")
    @Test
    void addNewImageToItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(sampleItem));
        UUID uploadedImgId = UUID.randomUUID();
        when(imgService.assignItemToNewImage(sampleItem, imgFile))
                .thenReturn(uploadedImgId);

        UUID result = itemService.addNewImageToItem(itemId, imgFile);

        assertEquals(uploadedImgId, result,
                "The returned ID should match the generated image ID");
        verify(itemRepository, times(1))
                .findById(itemId);
        verify(imgService, times(1))
                .assignItemToNewImage(sampleItem, imgFile);
    }

    @DisplayName("Add New Image to Item - Invalid Id")
    @Test
    void addNewImageToNonExistantItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class,
                () -> itemService.addNewImageToItem(itemId, imgFile));
    }

    @DisplayName("Add Exisiting Image to Item - Successful")
    @Test
    void addExistingImageToItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(sampleItem));
        UUID uploadedImgId = UUID.randomUUID();
        when(imgService.assignItemToExistingImage(sampleItem, uploadedImgId))
                .thenReturn(uploadedImgId);

        UUID result = itemService.addExistingImageToItem(itemId, uploadedImgId);

        assertEquals(uploadedImgId, result,
                "The returned ID should match the generated image ID");
        verify(itemRepository, times(1))
                .findById(any(UUID.class));
        verify(imgService, times(1))
                .assignItemToExistingImage(sampleItem, uploadedImgId);
    }

    @DisplayName("Add Exisiting Image to Item - Invalid Item Id")
    @Test
    void addNonExistantImageToItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        UUID uploadedImgId = UUID.randomUUID();
        assertThrows(ItemNotFoundException.class,
                () -> itemService.addExistingImageToItem(itemId, uploadedImgId));
    }

    @DisplayName("Get All Images - Successful")
    @Test
    void getAllImagesTest() {
        UUID imgId = UUID.randomUUID();
        Item updatedItemMock = mock(Item.class);
        Image steelCanImageMock = mock(Image.class);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(updatedItemMock));
        when(updatedItemMock.getImages())
                .thenReturn(List.of(steelCanImageMock));
        when(steelCanImageMock.getImageId())
                .thenReturn(imgId);
        when(imgService.getImageById(any(UUID.class)))
                .thenReturn(steelCanImage.getImageData());

        List<String> images = itemService.getAllImages(itemId);
        assertEquals(1, images.size());
        assertTrue(images.stream().anyMatch(image -> image.equals(BASE_64_STEEL_CAN_IMG)));
    }

    @DisplayName("Get All Images - Invalid Id")
    @Test
    void getAllImagesFromNonExistantItemTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class,
                () -> itemService.getAllImages(itemId));
    }

    @DisplayName("Get All Images Details - Successful")
    @Test
    void getAllImagesDetailsTest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(updatedItem));

        List<Image> images = itemService.getAllImagesDetails(itemId);
        assertEquals(2, images.size());
        Image img1 = images.get(0);
        assertEquals(steelCanImage.getImageName(), img1.getImageName());
        assertEquals(steelCanImage.getImageType(), img1.getImageType());
        assertArrayEquals(steelCanImage.getImageData(), img1.getImageData());
        Image img2 = images.get(1);
        assertEquals(steelCanSideImage.getImageName(), img2.getImageName());
        assertEquals(steelCanSideImage.getImageType(), img2.getImageType());
        assertArrayEquals(steelCanSideImage.getImageData(), img2.getImageData());
        verify(itemRepository, times(1)).findById(any(UUID.class));
    }
}
