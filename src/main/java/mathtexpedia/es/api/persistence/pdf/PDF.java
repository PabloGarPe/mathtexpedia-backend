package mathtexpedia.es.api.persistence.pdf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("pdf_id")
    private long id;

    @NotBlank
    @Column(unique = true)
    @JsonProperty("pdf_name")
    private String name;

    @JsonIgnore
    @Column(name = "s3_key")
    private String s3Key;

    @JsonProperty("pdf_last_time_edited")
    private Date lastTimeEdited;

    @JsonProperty("pdf_description")
    private String description;

    @ManyToOne
    @JoinColumn(name ="subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "subject_unit_id")
    private SubjectUnit subjectUnit;
}
