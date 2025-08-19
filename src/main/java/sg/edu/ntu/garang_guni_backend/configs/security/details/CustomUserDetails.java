package sg.edu.ntu.garang_guni_backend.configs.security.details;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String displayName;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public CustomUserDetails(UUID id, String displayName, String email, String password,
            Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    /**
     * Returns the user's uuid.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the user's authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the user's password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's username.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the unique identifier used for authentication.
     * <p>
     * Note: Although it's named getUsername(), this returns the user's email
     * because the application uses email as its primary login ID.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * This application does not use this function. Always returns true for now.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * This application does not use this function. Always returns true for now.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * This application does not use this function. Always returns true for now.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * This application does not use this function. Always returns true for now.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
