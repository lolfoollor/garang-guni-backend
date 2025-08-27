package sg.edu.ntu.garang_guni_backend.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.exceptions.image.ImageNotFoundException;
import sg.edu.ntu.garang_guni_backend.exceptions.image.ImageUnsupportedTypeException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "testuser", roles = { "USER" })
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static MockMultipartFile fakeImgFile;
    private static MockMultipartFile updatedImgFile;
    private static final String FAKE_ID = "00000000-0000-0000-0000-000000000000";
    private static final String PROBLEM_DETAIL_CONTENT_TYPE = "application/problem+json";

    @BeforeAll
    void setUp() {
        fakeImgFile = new MockMultipartFile(
                "image",
                "test_image.png",
                "image/png",
                "This is a test image".getBytes());

        updatedImgFile = new MockMultipartFile(
                "image",
                "test_image2.png",
                "image/png",
                "This is a test image2".getBytes());
    }

    @DisplayName("Upload Image - Successful")
    @Test
    void uploadImageTest() throws Exception {

        RequestBuilder createRequest = MockMvcRequestBuilders
                .multipart("/images").file(fakeImgFile);

        mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @DisplayName("Get Image By Name - Successful")
    @Test
    void getImageByNameTest() throws Exception {
        RequestBuilder createRequest = MockMvcRequestBuilders
                .multipart("/images").file(fakeImgFile);

        mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        RequestBuilder getRequest = MockMvcRequestBuilders
                .get("/images?fileName=" + fakeImgFile.getOriginalFilename());

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(fakeImgFile.getBytes()));
    }

    @DisplayName("Get Image By ID - Successful")
    @Test
    void getImageByIdTest() throws Exception {
        RequestBuilder createRequest = MockMvcRequestBuilders
                .multipart("/images").file(fakeImgFile);

        String unFormattedId = mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse().getContentAsString();

        // Get Content As String -> gives extra ""
        String id = unFormattedId.replaceAll("^\"|\"$", "");

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/images/" + id);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(fakeImgFile.getBytes()));
    }

    @DisplayName("Update Image - Successful")
    @Test
    void updateImageTest() throws Exception {
        RequestBuilder createRequest = MockMvcRequestBuilders
                .multipart("/images").file(fakeImgFile);

        String unFormattedId = mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Get Content As String -> gives extra ""
        String id = unFormattedId.replaceAll("^\"|\"$", "");

        RequestBuilder updateRequest = MockMvcRequestBuilders
                .multipart(HttpMethod.PUT, "/images/" + id).file(updatedImgFile);

        mockMvc.perform(updateRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(unFormattedId));
    }

    @DisplayName("Delete Image - Successful")
    @Test
    void deleteImageTest() throws Exception {
        RequestBuilder createRequest = MockMvcRequestBuilders
                .multipart("/images").file(fakeImgFile);

        String unFormattedId = mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Get Content As String -> gives extra ""
        String id = unFormattedId.replaceAll("^\"|\"$", "");

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/images/" + id);

        mockMvc.perform(deleteRequest).andExpect(status().isNoContent());

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/images/" + id);

        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }

    @DisplayName("Upload Image - Invalid file")
    @Test
    void uploadInvalidFileTypeTest() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test_file.txt",
                "text/plain",
                "This is a text file".getBytes());

        RequestBuilder request = MockMvcRequestBuilders
                .multipart("/images").file(invalidFile);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof ImageUnsupportedTypeException));
    }

    @DisplayName("Get Image By ID - Invalid Id")
    @Test
    void getNonExistentImageByIdTest() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/images/" + FAKE_ID);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof ImageNotFoundException));
    }

    @DisplayName("Update Image - Invalid Id")
    @Test
    void updateNonExistentImageTest() throws Exception {
        RequestBuilder updateRequest = MockMvcRequestBuilders
                .multipart(HttpMethod.PUT, "/images/" + FAKE_ID).file(updatedImgFile);

        mockMvc.perform(updateRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof ImageNotFoundException));
    }

    @DisplayName("Delete Image - Invalid Id")
    @Test
    void deleteNonExistentImageTest() throws Exception {
        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/images/" + FAKE_ID);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_DETAIL_CONTENT_TYPE))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof ImageNotFoundException));
    }
}
