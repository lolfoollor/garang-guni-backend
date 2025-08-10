package sg.edu.ntu.garang_guni_backend.mappers;

import org.springframework.stereotype.Component;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;
import sg.edu.ntu.garang_guni_backend.entities.User;

@Component
public class AuthRequestUserMapper {
    public User toEntity(RegistrationRequest registrationRequestDto) {
        User convertedUser = new User();

        convertedUser.setEmail(registrationRequestDto.getEmail());
        convertedUser.setUsername(registrationRequestDto.getUsername());
        convertedUser.setPassword(registrationRequestDto.getPassword());

        return convertedUser;
    }

}
