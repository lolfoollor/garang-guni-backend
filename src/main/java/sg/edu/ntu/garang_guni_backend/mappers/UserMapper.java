package sg.edu.ntu.garang_guni_backend.mappers;

import org.springframework.stereotype.Component;
import sg.edu.ntu.garang_guni_backend.dtos.UserProfileResponse;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;

@Component
public class UserMapper {
    public UserProfileResponse toUserProfileDto(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRoles().stream().map(UserRole::getRoleName).toList());
    }
}
