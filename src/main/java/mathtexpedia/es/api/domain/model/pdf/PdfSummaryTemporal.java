package mathtexpedia.es.api.domain.model.pdf;
//TODO: Eliminar esto cuando esté la implementación de verdad, es para poder hacer algo sobre le chat

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PdfSummaryTemporal {

    private String name;
    private String tag;

    @Nullable
    private String link;
}
