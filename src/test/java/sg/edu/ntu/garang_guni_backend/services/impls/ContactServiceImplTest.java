package sg.edu.ntu.garang_guni_backend.services.impls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.ntu.garang_guni_backend.entities.Contact;
import sg.edu.ntu.garang_guni_backend.repositories.ContactRepository;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private Contact createSampleContact() {
        Contact contact = new Contact();
        contact.setFirstName("Wong");
        contact.setLastName("Wang");
        contact.setEmail("wong@gmail.com");
        contact.setPhoneNumber("+6598765432");
        contact.setSubject("feedback from wong");
        contact.setMessageContent("This is a wong test feedback.");
        contact.setCaseId(UUID.randomUUID());
        return contact;
    }

    @Test
    void createContactTest() {

        Contact contact = createSampleContact();

        when(contactRepository.save(contact)).thenReturn(contact);

        Contact savedContact = contactService.createContact(contact);

        assertEquals(contact.getFirstName(), savedContact.getFirstName());
        assertEquals(contact.getLastName(), savedContact.getLastName());
        assertEquals(contact.getEmail(), savedContact.getEmail());
        assertEquals(contact.getPhoneNumber(), savedContact.getPhoneNumber());
        assertEquals(contact.getSubject(), savedContact.getSubject());
        assertEquals(contact.getMessageContent(), savedContact.getMessageContent());
        verify(contactRepository, times(1)).save(contact);
    }

    @Test
    void createContactWithSanitizationTest() {

        Contact contact = createSampleContact();

        contact.setMessageContent("Hello <script>alert('XSS');</script> World!");

        Contact sanitizedContact = createSampleContact();
        sanitizedContact.setMessageContent("Hello  World!");
        when(contactRepository.save(contact)).thenReturn(sanitizedContact);

        Contact savedContact = contactService.createContact(contact);

        assertEquals("Hello  World!", savedContact.getMessageContent());
        verify(contactRepository, times(1)).save(contact);
    }

    @Test
    void createContactWithEmptyMessageAfterSanitizationTest() {

        Contact contact = createSampleContact();

        contact.setMessageContent("<script>alert('XSS');</script>");

        Exception exception = assertThrows(ValidationException.class, () -> {
            contactService.createContact(contact);
        });

        assertEquals("Message content cannot be empty after sanitization", exception.getMessage());
    }

    @Test
    void getAllContactsTest() {

        Contact contact = createSampleContact();
        when(contactRepository.findAll()).thenReturn(List.of(contact));

        List<Contact> contacts = contactService.getAllContacts();

        assertEquals(1, contacts.size(), "Should be 1 contact only in the list.");
        assertEquals(contact.getFirstName(), contacts.get(0).getFirstName());
        assertEquals(contact.getLastName(), contacts.get(0).getLastName());
        assertEquals(contact.getEmail(), contacts.get(0).getEmail());
        assertEquals(contact.getPhoneNumber(), contacts.get(0).getPhoneNumber());
        assertEquals(contact.getSubject(), contacts.get(0).getSubject());
        assertEquals(contact.getMessageContent(), contacts.get(0).getMessageContent());
    }

    @Test
    void getEmptyContactsTest() {
        when(contactRepository.findAll()).thenReturn(Collections.emptyList());

        List<Contact> contacts = contactService.getAllContacts();

        assertEquals(0, contacts.size(), "This should be an empty list.");
    }
}
