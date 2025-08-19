package sg.edu.ntu.garang_guni_backend.dtos;

import java.util.List;
import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String username, List<String> roles) {
    public UserProfileResponse {
        roles = List.copyOf(roles);
    }
}
