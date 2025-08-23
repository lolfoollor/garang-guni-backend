package sg.edu.ntu.garang_guni_backend.configs.security.services;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sg.edu.ntu.garang_guni_backend.configs.EmailConfig;
import sg.edu.ntu.garang_guni_backend.configs.security.details.CustomUserDetails;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.exceptions.email.EmailNotVerifiedException;
import sg.edu.ntu.garang_guni_backend.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final EmailConfig emailConfig;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository, EmailConfig emailConfig) {
        this.userRepository = userRepository;
        this.emailConfig = emailConfig;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User selectedUser = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        if (emailConfig.isVerificationRequired() && !selectedUser.isEmailVerified()) {
            throw new EmailNotVerifiedException(email);
        }

        List<GrantedAuthority> authorities = selectedUser.getRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRoleName()))
                .collect(Collectors.toList());

        return new CustomUserDetails(
                selectedUser.getId(),
                selectedUser.getUsername(),
                selectedUser.getEmail(),
                selectedUser.getPassword(),
                authorities,
                selectedUser.isEnabled());
    }
}
