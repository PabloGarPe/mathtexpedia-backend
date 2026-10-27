package mathtexpedia.es.api.infrastructure.adapter.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationDecision;

record ModelVerdict(
        @JsonProperty(required = true) ModerationDecision decision,
        @JsonProperty(required = true) ModelCategory category
        ) {

    enum ModelCategory {
        HATE_SPEECH,
        HARASSMENT,
        SEXUAL_CONTENT,
        VIOLENCE_OR_THREATS,
        ILLEGAL_ACTIVITY,
        SPAM,
        PERSONAL_DATA,
        OTHER,
        NONE
    }
}
