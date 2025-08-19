package sg.edu.ntu.garang_guni_backend.services.impls;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import sg.edu.ntu.garang_guni_backend.configs.security.details.CustomUserDetails;
import sg.edu.ntu.garang_guni_backend.configs.security.services.JwtService;
import sg.edu.ntu.garang_guni_backend.dtos.AuthResponse;
import sg.edu.ntu.garang_guni_backend.dtos.JwtUserDetails;
import sg.edu.ntu.garang_guni_backend.dtos.LoginRequest;
import sg.edu.ntu.garang_guni_backend.dtos.RegistrationRequest;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.entities.UserRole;
import sg.edu.ntu.garang_guni_backend.mappers.AuthRequestUserMapper;
import sg.edu.ntu.garang_guni_backend.services.AuthService;
import sg.edu.ntu.garang_guni_backend.services.UserService;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AuthRequestUserMapper authRequestUserMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(UserService userService, AuthenticationManager authenticationManager,
            AuthRequestUserMapper authRequestUserMapper, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.authRequestUserMapper = authRequestUserMapper;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegistrationRequest registrationRequest) {
        logger.info("Register request received for email: {}", registrationRequest.getEmail());

        User userToCreate = authRequestUserMapper.toEntity(registrationRequest);
        User newUser = userService.createUser(userToCreate);
        logger.info("User created with ID: {}", newUser.getId());

        JwtUserDetails details = new JwtUserDetails(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getRoles().stream().map(UserRole::getRoleName).toList());
        String jwtToken = jwtService.generateToken(details);

        return new AuthResponse(jwtToken, "Bearer");
    }

    @Override
    public AuthResponse authenticate(LoginRequest loginRequest) {
        logger.info("Authentication attempt for email: {}", loginRequest.getEmail());
        UsernamePasswordAuthenticationToken authToken = UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .toList();
        JwtUserDetails jwtDetails = new JwtUserDetails(
                userDetails.getId(),
                userDetails.getDisplayName(),
                userDetails.getUsername(),
                roles);
        String jwtToken = jwtService.generateToken(jwtDetails);
        logger.info("Authentication successful for email: {}", loginRequest.getEmail());

        return new AuthResponse(jwtToken, "Bearer");
    }
}
