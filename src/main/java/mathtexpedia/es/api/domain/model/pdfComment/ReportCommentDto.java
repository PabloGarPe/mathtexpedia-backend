package mathtexpedia.es.api.domain.model.pdfComment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCommentDto {

    @NotNull
    private ReportReason reason;
}
