package sg.edu.ntu.garang_guni_backend.dtos;

import java.util.List;
import java.util.UUID;

public record JwtUserDetails(UUID id, String username, String email, List<String> roles) {
    public JwtUserDetails {
        roles = List.copyOf(roles);
    }
}
