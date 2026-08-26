package mathtexpedia.es.api.domain.model.pdf;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PDFSummary {
    private String name;
    private String tag;
    @Nullable
    private String link;
}
