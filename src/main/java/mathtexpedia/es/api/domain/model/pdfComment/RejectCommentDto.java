package mathtexpedia.es.api.domain.model.pdfComment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectCommentDto {

    @NotNull
    private ModerationCategory category;
}
