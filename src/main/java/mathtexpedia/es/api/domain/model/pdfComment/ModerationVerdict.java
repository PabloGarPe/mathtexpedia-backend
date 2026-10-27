package mathtexpedia.es.api.domain.model.pdfComment;

public record ModerationVerdict(
        ModerationDecision decision,
        ModerationCategory category
) {
}
