package sg.edu.ntu.garang_guni_backend.exceptions.auth;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String msg) {
        super("Access denied: " + msg);
    }
}
