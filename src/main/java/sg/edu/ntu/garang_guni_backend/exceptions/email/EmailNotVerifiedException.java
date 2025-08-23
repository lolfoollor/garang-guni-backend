package sg.edu.ntu.garang_guni_backend.exceptions.email;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String email) {
        super("Your email (%s) is not verified".formatted(email));
    }
}
