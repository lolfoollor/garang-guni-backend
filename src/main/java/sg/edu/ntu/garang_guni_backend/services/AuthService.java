package sg.edu.ntu.garang_guni_backend.services;

import sg.edu.ntu.garang_guni_backend.dtos.AuthResponse;
import sg.edu.ntu.garang_guni_backend.dtos.LoginRequest;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;

public interface AuthService {
    AuthResponse register(RegistrationRequest registrationRequest);

    AuthResponse authenticate(LoginRequest loginRequest);
}
