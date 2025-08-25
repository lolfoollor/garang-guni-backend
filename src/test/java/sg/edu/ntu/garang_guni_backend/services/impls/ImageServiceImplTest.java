package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import sg.edu.ntu.garang_guni_backend.entities.Image;
import sg.edu.ntu.garang_guni_backend.entities.Item;
import sg.edu.ntu.garang_guni_backend.exceptions.image.ImageNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.image.ImageUnsupportedTypeException;
import sg.edu.ntu.garang_guni_backend.repositories.ImageRepository;
import sg.edu.ntu.garang_guni_backend.utils.ImageUtils;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {
    @Mock
    private ImageRepository imgRepository;

    @InjectMocks
    private ImageServiceImpl imgService;

    @Mock
    private MultipartFile file;

    @Test
    @DisplayName("Upload Image - Successful")
    void uploadImageTest() throws Exception {
        when(file.getOriginalFilename()).thenReturn("test_image.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("image_data".getBytes());

        UUID imageId = UUID.randomUUID();
        when(imgRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image img = invocation.getArgument(0);
            img.setImageId(imageId);
            return img;
        });

        UUID savedImgUuid = imgService.uploadImage(file);
        assertEquals(imageId, savedImgUuid,
                "The saved Image id should be the same as the new Image id");

        verify(imgRepository, times(1)).save(any(Image.class));
    }

    @DisplayName("Upload Image - Invalid file")
    @Test
    void uploadInvalidFileTypeTest() {
        assertThrows(ImageUnsupportedTypeException.class, () -> {
            imgService.uploadImage(file);
        });
    }

    @DisplayName("Get Image By Name - Successful")
    @Test
    void getImageByNameTest() throws Exception {
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setImageId(id);
        image.setImageName("test_image.png");
        image.setImageData(ImageUtils.compressImage("compressed_image_data".getBytes()));

        when(imgRepository.findByImageName("test_image.png")).thenReturn(Optional.of(image));

        byte[] result = imgService.getImageByName("test_image.png");

        verify(imgRepository, times(1)).findByImageName("test_image.png");
        assertArrayEquals("compressed_image_data".getBytes(), result);
    }

    @DisplayName("Get Image By Name - Invalid Id")
    @Test
    void getNonExistentImageByNameTest() {
        when(imgRepository.findByImageName("test_image.png")).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> {
            imgService.getImageByName("test_image.png");
        });
    }

    @DisplayName("Get Image By ID - Successful")
    @Test
    void getImageByIdTest() throws Exception {
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setImageId(id);
        image.setImageData(ImageUtils.compressImage("compressed_image_data".getBytes()));

        when(imgRepository.findById(id)).thenReturn(Optional.of(image));

        byte[] result = imgService.getImageById(id);

        verify(imgRepository, times(1)).findById(id);
        assertArrayEquals("compressed_image_data".getBytes(), result);
    }

    @DisplayName("Get Image By ID - Invalid Id")
    @Test
    void getNonExistentImageByIdTest() {
        UUID id = UUID.randomUUID();
        when(imgRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> {
            imgService.getImageById(id);
        });
    }

    @DisplayName("Update Image - Successful")
    @Test
    void updateImageTest() throws Exception {
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setImageId(id);

        when(imgRepository.findById(id)).thenReturn(Optional.of(image));
        when(file.getOriginalFilename()).thenReturn("test_image.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("updated_image_data".getBytes());
        when(imgRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image img = invocation.getArgument(0);
            img.setImageId(id);
            return img;
        });

        UUID result = imgService.updateImage(id, file);

        verify(imgRepository, times(1)).save(any(Image.class));
        assertEquals(id, result);
    }

    @DisplayName("Update Image - Invalid file")
    @Test
    void updateImageUnsupportedTypeTest() {
        UUID id = UUID.randomUUID();
        assertThrows(ImageUnsupportedTypeException.class, () -> {
            imgService.updateImage(id, file);
        });
    }

    @DisplayName("Update Image - Invalid Id")
    @Test
    void updateNonExistentImageTest() {
        UUID id = UUID.randomUUID();
        when(imgRepository.findById(id)).thenReturn(Optional.empty());
        when(file.getOriginalFilename()).thenReturn("test_image.png");

        assertThrows(ImageNotFoundException.class, () -> {
            imgService.updateImage(id, file);
        });
    }

    @DisplayName("Delete Image - Successful")
    @Test
    void deleteImageTest() {
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setImageId(id);

        when(imgRepository.findById(id)).thenReturn(Optional.of(image));

        UUID result = imgService.deleteImage(id);

        verify(imgRepository, times(1)).deleteById(id);
        assertEquals(id, result);
    }

    @DisplayName("Delete Image - Invalid Id")
    @Test
    void deleteNonExistentImageTest() {
        UUID id = UUID.randomUUID();
        when(imgRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> {
            imgService.deleteImage(id);
        });
    }

    @Test
    @DisplayName("Upload Image And Assign ItemId - Successful")
    void uploadImageAndAssignItemIdTest() throws Exception {
        when(file.getOriginalFilename()).thenReturn("test_image.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("image_data".getBytes());

        UUID imageId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Item item = Item.builder()
                .itemId(itemId)
                .itemName("Test item")
                .itemDescription("It's just an Item.")
                .build();

        List<Image> imagesStored = new ArrayList<>();

        when(imgRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image img = invocation.getArgument(0);
            img.setImageId(imageId);
            imagesStored.add(img);
            return img;
        });

        UUID savedImgUuid = imgService.assignItemToNewImage(item, file);
        assertEquals(imageId, savedImgUuid,
                "The saved Image id should be the same as the new Image id");

        when(imgRepository.findById(savedImgUuid))
                .thenReturn(imagesStored.stream()
                        .filter(image -> image.getImageId().equals(savedImgUuid))
                        .findFirst());

        UUID randomItemId = UUID.randomUUID();
        assertTrue(imgService.isLinked(savedImgUuid.toString(), itemId.toString()));
        verify(imgRepository, times(1)).save(any(Image.class));
        assertFalse(imgService.isLinked(savedImgUuid.toString(), randomItemId.toString()));
    }

    @Test
    @DisplayName("Upload Image And Assign ItemId - Invalid image file")
    void uploadFileAndAssignItemIdTest() throws Exception {
        Item item = Item.builder()
                .itemName("Test item")
                .itemDescription("It's just an Item.")
                .build();

        assertThrows(ImageUnsupportedTypeException.class,
                () -> imgService.assignItemToNewImage(item, file));
    }

    @Test
    @DisplayName("Assign Item To Existing Image - Successful")
    void assignItemToExistingImageTest() throws Exception {
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setImageId(id);
        image.setImageName("test_image.png");
        image.setImageData(ImageUtils.compressImage("compressed_image_data".getBytes()));

        Item item = Item.builder()
                .itemName("Test item")
                .itemDescription("It's just an Item.")
                .build();

        when(imgRepository.findById(id)).thenReturn(Optional.of(image));
        when(imgRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UUID imageId = imgService.assignItemToExistingImage(item, id);

        assertEquals(id, imageId);
        verify(imgRepository, times(1)).save(any(Image.class));
        verify(imgRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Assign Item To Existing Image - Invalid image Id")
    void assignItemToNonExistingImageTest() {
        UUID id = UUID.randomUUID();

        Item item = Item.builder()
                .itemName("Test item")
                .itemDescription("It's just an Item.")
                .build();

        when(imgRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class,
                () -> imgService.assignItemToExistingImage(item, id));
    }

    @Test
    @DisplayName("Is Linked - Successful")
    void isLinkedTest() {
        UUID imgId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        Item item = Item.builder()
                .itemId(itemId)
                .itemName("Test item")
                .itemDescription("It's just an Item.")
                .build();

        Image img = Image.builder()
                .imageId(imgId)
                .imageName("Test.png")
                .imageType("image/png")
                .imageData("Test_Img".getBytes())
                .item(item)
                .build();

        when(imgRepository.findById(imgId)).thenReturn(Optional.of(img));

        assertTrue(imgService.isLinked(imgId.toString(), itemId.toString()));
        assertFalse(imgService.isLinked(imgId.toString(), itemId2.toString()));
    }
}
