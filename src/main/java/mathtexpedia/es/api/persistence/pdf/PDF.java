package mathtexpedia.es.api.persistence.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mathtexpedia.es.api.domain.model.pdf.PDFTag;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class PDF {

    @Schema(description = "Identificador interno del PDF", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("pdf_id")
    private long id;

    @Schema(description = "Nombre único del PDF dentro del catálogo")
    @NotBlank
    @Column(unique = true)
    @JsonProperty("pdf_name")
    private String name;

    @Schema(description = "Enlace de descarga del PDF")
    @Nullable
    @JsonProperty("pdf_link")
    private String link;

    @Schema(description = "Fecha de la última edición del PDF", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("pdf_last_time_edited")
    private Date lastTimeEdited;

    @Schema(description = "Descripción del contenido del PDF")
    @JsonProperty("pdf_description")
    private String description;

    @Schema(description = "Categoría a la que pertenece el PDF")
    @JsonProperty("pdf_tags")
    @Enumerated(EnumType.STRING)
    private PDFTag tag;

    public PDF(long id, String name, Date lastTimeEdited, String description, PDFTag tag) {
        this.id = id;
        this.name = name;
        this.lastTimeEdited = lastTimeEdited;
        this.description = description;
        this.tag = tag;
    }

}
