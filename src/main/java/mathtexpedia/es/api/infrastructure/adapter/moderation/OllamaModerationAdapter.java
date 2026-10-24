package mathtexpedia.es.api.infrastructure.adapter.moderation;

import mathtexpedia.es.api.domain.exception.ModerationInvalidOutputException;
import mathtexpedia.es.api.domain.exception.ModerationUnavailableException;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationVerdict;
import mathtexpedia.es.api.domain.port.moderation.CommentModerationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OllamaModerationAdapter implements CommentModerationPort {

    private static final Logger logger = LoggerFactory.getLogger(OllamaModerationAdapter.class);

    @Override
    public ModerationVerdict moderate(String text) throws ModerationUnavailableException, ModerationInvalidOutputException {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
