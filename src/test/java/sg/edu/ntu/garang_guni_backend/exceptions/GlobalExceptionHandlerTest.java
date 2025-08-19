package sg.edu.ntu.garang_guni_backend.exceptions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.entities.Contact;
import sg.edu.ntu.garang_guni_backend.services.ContactService;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = { "USER" })
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @Test
    @DisplayName("Test for handling ContactNotProcessingException")
    void testHandleContactNotProcessingException() throws Exception {
        doThrow(new ContactNotProcessingException("Processing error"))
                .when(contactService).createContact(any(Contact.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"firstName\": \"Wang\", \"email\": \"wang@gmail.com\","
                        + " \"phoneNumber\": \"+6598765432\", \"subject\": \"Test\","
                        + " \"messageContent\": \"This is a test message.\" }"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Processing error"));
    }

    @Test
    @DisplayName("Test for handling generic exceptions")
    void testHandleGenericException() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(contactService).createContact(any(Contact.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"firstName\": \"Wang\", \"email\": \"wang@gmail.com\","
                        + " \"phoneNumber\": \"+6598765432\", \"subject\": \"Test\","
                        + " \"messageContent\": \"This is a test message.\" }"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(
                        "An error occurred. Please contact support."));
    }
}
