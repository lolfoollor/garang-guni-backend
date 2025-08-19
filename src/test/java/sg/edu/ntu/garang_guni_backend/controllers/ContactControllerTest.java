package sg.edu.ntu.garang_guni_backend.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sg.edu.ntu.garang_guni_backend.entities.Contact;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Test for creating a valid contact form")
    @Test
    void validContactCreationTest() throws Exception {
        Contact newContact = new Contact();
        newContact.setFirstName("Wang");
        newContact.setLastName("Wong");
        newContact.setEmail("wang@gmail.com");
        newContact.setPhoneNumber("+6598765432");
        newContact.setSubject("Inquiry");
        newContact.setMessageContent("This is a test message.");

        String newContactAsJson = objectMapper.writeValueAsString(newContact);

        RequestBuilder request = MockMvcRequestBuilders.post("/contacts")
                .contentType(MediaType.APPLICATION_JSON).content(newContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Wang"))
                .andExpect(jsonPath("$.lastName").value("Wong"))
                .andExpect(jsonPath("$.email").value("wang@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+6598765432"))
                .andExpect(jsonPath("$.subject").value("Inquiry"))
                .andExpect(jsonPath("$.messageContent").value("This is a test message."));
    }

    @DisplayName("Test creating contact with empty First and Last Name")
    @Test
    void emptyNameContactCreationTest() throws Exception {
        Contact emptyNameContact = new Contact();
        emptyNameContact.setFirstName("");
        emptyNameContact.setLastName("");
        emptyNameContact.setEmail("wang@gmail.com");
        emptyNameContact.setPhoneNumber("+6598765432");
        emptyNameContact.setSubject("valid subject");
        emptyNameContact.setMessageContent("Message with valid content.");

        String emptyNameContactAsJson = objectMapper.writeValueAsString(emptyNameContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyNameContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Test creating valid contact with first name only")
    @Test
    void validContactWithFirstNameOnlyTest() throws Exception {
        Contact newContact = new Contact();
        newContact.setFirstName("Wang");
        newContact.setLastName("");
        newContact.setEmail("wang@gmail.com");
        newContact.setPhoneNumber("+6592345678");
        newContact.setSubject("Inquiry");
        newContact.setMessageContent("This is a test message.");

        String newContactAsJson = objectMapper.writeValueAsString(newContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON).content(newContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Wang"))
                .andExpect(jsonPath("$.lastName").value(""));
    }

    @DisplayName("Test creating contact with long firstName")
    @Test
    void tooLongFirstNameContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("ThisNameIsDefinitelyWayTooLong");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6512345678");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("Test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating valid contact with last name only")
    @Test
    void validContactWithLastNameOnlyTest() throws Exception {
        Contact newContact = new Contact();
        newContact.setFirstName("");
        newContact.setLastName("Wong");
        newContact.setEmail("wong@gmail.com");
        newContact.setPhoneNumber("+6592345678");
        newContact.setSubject("Inquiry");
        newContact.setMessageContent("This is a test message.");

        String newContactAsJson = objectMapper.writeValueAsString(newContact);

        RequestBuilder request = MockMvcRequestBuilders.post("/contacts")
                .contentType(MediaType.APPLICATION_JSON).content(newContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastName").value("Wong"))
                .andExpect(jsonPath("$.firstName").value(""));
    }

    @DisplayName("Test creating contact with too long lastName")
    @Test
    void tooLongLastNameContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("ThisLastNameIsWayTooLongToBeValid");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("This is a test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders.post("/contacts")
                .contentType(MediaType.APPLICATION_JSON).content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with empty phoneNumber and email")
    @Test
    void emptyPhoneAndEmailContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("");
        invalidContact.setPhoneNumber("");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("Test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON));
    }

    @DisplayName("Test creating contact with invalid phone number")
    @Test
    void invalidPhoneNumberContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("123456");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("Test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .exists());
    }

    @DisplayName("Test creating contact with invalid email")
    @Test
    void invalidEmailContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("gg.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("Test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with empty subject")
    @Test
    void emptySubjectContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("");
        invalidContact.setMessageContent("This is a test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with too long subject")
    @Test
    void tooLongSubjectContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("ThisSubjectIsWayTooLongToBeValid");
        invalidContact.setMessageContent("This is a test message.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with empty messageContent")
    @Test
    void emptyMessageContentContactCreationTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with very long message content")
    @Test
    void longMessageContentTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("Inquiry");
        invalidContact.setMessageContent("This is a very long test message "
                + "that exceeds the 200 character limit. I also dont know what"
                + "else should write to make it more then 200 character. Maybe I"
                + "should just limit user to key in 100 character and that it."
                + "I think by now should mbe more than enough. lets see the "
                + "test result.");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with empty request body")
    @Test
    void emptyContactRequestTest() throws Exception {
        String emptyContactAsJson = "{}";

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Test creating contact with XSS attack in message content")
    @Test
    void xssAttackOnlyMessageContentTest() throws Exception {
        Contact invalidContact = new Contact();
        invalidContact.setFirstName("Wang");
        invalidContact.setLastName("Wong");
        invalidContact.setEmail("wang@gmail.com");
        invalidContact.setPhoneNumber("+6592345678");
        invalidContact.setSubject("Inquiry");

        invalidContact.setMessageContent("<script>alert('XSS');</script>");

        String invalidContactAsJson = objectMapper.writeValueAsString(invalidContact);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidContactAsJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }
}
