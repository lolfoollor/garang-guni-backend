package sg.edu.ntu.garang_guni_backend.exceptions.email;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException(String email) {
        super("User with email (%s) is already verified".formatted(email));
    }
}
