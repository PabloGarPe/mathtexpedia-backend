package mathtexpedia.es.api.domain.exception;

public class ModerationInvalidOutputException extends Exception {
    public ModerationInvalidOutputException(String message) {
        super(message);
    }
    public ModerationInvalidOutputException(String message, Throwable cause) {
        super(message, cause);
    }
}
