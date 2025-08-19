package sg.edu.ntu.garang_guni_backend.configs.security.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sg.edu.ntu.garang_guni_backend.configs.security.details.CustomUserDetails;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static User user;

    @BeforeAll
    static void init() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .email("testuser@example.com")
                .password("encodedPassword")
                .roles(Set.of(UserRole.USER))
                .isEnabled(true)
                .build();
    }

    @DisplayName("Load user by username - Success")
    @Test
    void loadUserByUsernameTest() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        assertNotNull(customUserDetails);
        assertEquals(user.getUsername(),
                customUserDetails.getDisplayName(), "User's username should match");
        assertEquals(user.getEmail(),
                customUserDetails.getUsername(), "User's email should match");
        assertEquals(user.getPassword(),
                customUserDetails.getPassword(), "User's password should match");
        assertEquals(1,
                customUserDetails.getAuthorities().size(), "User's should have one role");
        assertEquals(List.of("ROLE_USER"),
                customUserDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                "User role should be USER");

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @DisplayName("Load user by username - User not found")
    @Test
    void loadInvalidUserByUsernameTest() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email));
        verify(userRepository, times(1)).findByEmail(email);
    }
}
