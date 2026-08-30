package mathtexpedia.es.api.domain.exception;

public class MathtexpediaConflictException extends Exception {
    public MathtexpediaConflictException(String message) {
        super(message);
    }

    public MathtexpediaConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
