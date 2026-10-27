package mathtexpedia.es.api.domain.port.moderation;

import mathtexpedia.es.api.domain.exception.ModerationInvalidOutputException;
import mathtexpedia.es.api.domain.exception.ModerationUnavailableException;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationVerdict;

public interface CommentModerationPort {

    ModerationVerdict moderate(String text) throws ModerationUnavailableException, ModerationInvalidOutputException;
    boolean isAvailable();

}
