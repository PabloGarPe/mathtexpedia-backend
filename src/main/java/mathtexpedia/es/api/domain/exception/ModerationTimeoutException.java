package mathtexpedia.es.api.domain.exception;

public class ModerationTimeoutException extends ModerationUnavailableException {
    public ModerationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
