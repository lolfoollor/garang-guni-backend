package sg.edu.ntu.garang_guni_backend.exceptions.email;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
