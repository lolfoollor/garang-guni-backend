package sg.edu.ntu.garang_guni_backend.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageUtilsTest {

    @Mock
    private MultipartFile file;

    @DisplayName("Compress and Decompress Image - Success")
    @Test
    void compressAndDecompressImageTest() throws Exception {
        byte[] originalData = "This is some test image data".getBytes();
        byte[] compressedData = ImageUtils.compressImage(originalData);
        byte[] decompressedData = ImageUtils.decompressImage(compressedData);
        assertArrayEquals(originalData, decompressedData);
    }

    @DisplayName("isImage - Success")
    @Test
    void isImageTest() {
        when(file.getOriginalFilename()).thenReturn("test_image.jpg");
        assertTrue(ImageUtils.isImage(file));

        when(file.getOriginalFilename()).thenReturn("test_image.jpeg");
        assertTrue(ImageUtils.isImage(file));

        when(file.getOriginalFilename()).thenReturn("test_image.png");
        assertTrue(ImageUtils.isImage(file));

        when(file.getOriginalFilename()).thenReturn("test_image.gif");
        assertTrue(ImageUtils.isImage(file));

        when(file.getOriginalFilename()).thenReturn("test_image.bmp");
        assertTrue(ImageUtils.isImage(file));

        when(file.getOriginalFilename()).thenReturn("test_image.test.png");
        assertTrue(ImageUtils.isImage(file));

    }

    @DisplayName("isImage - Non Existant File or File Name")
    @Test
    void isImageNonExistantFileOrFileNameTest() {
        assertFalse(ImageUtils.isImage(null));

        when(file.getOriginalFilename()).thenReturn(null);
        assertFalse(ImageUtils.isImage(file));
    }

    @DisplayName("isImage - Invalid Extension")
    @Test
    void isImageInvalidExtensionTest() {
        when(file.getOriginalFilename()).thenReturn("test_image.apng");
        assertFalse(ImageUtils.isImage(file));

        when(file.getOriginalFilename()).thenReturn("test_image");
        assertFalse(ImageUtils.isImage(file));
    }

    @DisplayName("Convert Bytes[] To Base64")
    @Test
    void convertBytesArrToBase64Test() {
        byte[] sampleData = "This is a picture".getBytes();
        String result = ImageUtils.convertBytesArrToBase64(sampleData);
        String expectedStr = "VGhpcyBpcyBhIHBpY3R1cmU=";
        assertEquals(result, expectedStr, "The encoding must be the same");
    }
}
