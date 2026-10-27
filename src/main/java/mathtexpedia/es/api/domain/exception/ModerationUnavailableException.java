package mathtexpedia.es.api.domain.exception;

public class ModerationUnavailableException extends Exception {
    public ModerationUnavailableException(String message) {
        super(message);
    }
    public ModerationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
