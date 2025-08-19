package sg.edu.ntu.garang_guni_backend.services;

import java.util.UUID;
import sg.edu.ntu.garang_guni_backend.entities.User;

public interface UserService {
    User createUser(User user);

    User getUserById(UUID id);

    User getUserByEmail(String email);

    User updateUser(UUID id, User user);
}
