package sg.edu.ntu.garang_guni_backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.edu.ntu.garang_guni_backend.dtos.UserProfileResponse;
import sg.edu.ntu.garang_guni_backend.entities.User;
import sg.edu.ntu.garang_guni_backend.mappers.UserMapper;
import sg.edu.ntu.garang_guni_backend.services.EmailVerificationService;

@RestController
@RequestMapping("/api/verification")
public class EmailVerificationController {

    private EmailVerificationService emailVerificationService;
    private UserMapper userMapper;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService,
            UserMapper userMapper) {
        this.emailVerificationService = emailVerificationService;
        this.userMapper = userMapper;
    }

    @PostMapping("/email/resend-verification")
    public ResponseEntity<Void> resendVerificationLink(
            @RequestParam String email) {
        emailVerificationService.resendVerificationOtp(email);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/email/verify")
    public ResponseEntity<UserProfileResponse> verifyOtp(
            @RequestParam String email, @RequestParam String otp) {
        User verifiedUser = emailVerificationService.verifyEmail(email, otp);

        return ResponseEntity.status(HttpStatus.OK)
                .body(userMapper.toUserProfileDto(verifiedUser));
    }
}
