package mathtexpedia.es.api.infrastructure.adapter.moderation;

import mathtexpedia.es.api.domain.exception.ModerationInvalidOutputException;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationDecision;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationVerdict;

final class VerdictMapper {

    private VerdictMapper() {}

    static ModerationVerdict toDomain(ModelVerdict verdict) throws ModerationInvalidOutputException {
        if (verdict == null || verdict.decision() == null)
            throw new ModerationInvalidOutputException("The model returned no decision");

        ModerationCategory category = toCategory(verdict.category());

        return switch (verdict.decision()) {
            case OK -> new ModerationVerdict(ModerationDecision.OK, null);
            case REVIEW -> new ModerationVerdict(ModerationDecision.REVIEW, category);
            case REJECT -> new ModerationVerdict(ModerationDecision.REJECT, category != null ? category : ModerationCategory.OTHER);
        };
    }

    private static ModerationCategory toCategory(ModelVerdict.ModelCategory category) {
        if (category == null || category == ModelVerdict.ModelCategory.NONE)
            return null;
        return ModerationCategory.valueOf(category.name());
    }
}
