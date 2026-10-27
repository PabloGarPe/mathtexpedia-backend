package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.ModerationInvalidOutputException;
import mathtexpedia.es.api.domain.exception.ModerationUnavailableException;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationVerdict;
import mathtexpedia.es.api.domain.port.moderation.CommentModerationPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ResilientModerationPort implements CommentModerationPort {

    private final CommentModerationPort delegate;
    private final ModerationAvailability availability;

    public ResilientModerationPort(
            @Qualifier("ollamaModerationAdapter") CommentModerationPort delegate,
            ModerationAvailability moderationAvailability
    ) {
        this.delegate = delegate;
        this.availability = moderationAvailability;
    }

    @Override
    public ModerationVerdict moderate(String text) throws ModerationUnavailableException, ModerationInvalidOutputException {
        if (availability.isDown()){
            if (!availability.canAttempt()) throw new ModerationUnavailableException("AI moderation is down; next check not due yet");
            if (!delegate.isAvailable()) {
                availability.recordFailure();
                throw new ModerationUnavailableException("AI moderation is still down");
            }
            // Responde: se deja pasar esta llamada y su resultado decide si se cierra el breaker
        }

        try {
            ModerationVerdict verdict = delegate.moderate(text);
            availability.recordSuccess();
            return verdict;
        } catch (ModerationInvalidOutputException e) {
            availability.recordSuccess();
            throw e;
        } catch (ModerationUnavailableException e) {
            availability.recordFailure();
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }
}
