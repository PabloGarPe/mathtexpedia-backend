package mathtexpedia.es.api.persistence.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Constraint;
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
public class PDF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("pdf_id")
    private long id;

    @NotBlank
    @Column(unique = true)
    @JsonProperty("pdf_name")
    private String name;

    @Nullable
    @JsonProperty("pdf_link")
    private String link;

    @JsonProperty("pdf_last_time_edited")
    private Date lastTimeEdited;

    @JsonProperty("pdf_description")
    private String description;

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
