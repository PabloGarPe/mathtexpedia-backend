package mathtexpedia.es.api.domain.model.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePDFDto {

    @NotBlank
    private String name;
    @NotBlank
    private String link;
    @NotNull
    private PDFTag pdfTag;
    private String description;

    @Override
    public String toString() {
        return "PDF [name=" + name + ", link=" + link + ", pdfTag=" + pdfTag + ", description=" + description + "]";
    }
}
