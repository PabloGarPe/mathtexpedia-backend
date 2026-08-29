package mathtexpedia.es.api.persistence.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "subject")
@Data
public class Subject {

    @Schema(description = "Identificador interno de la asignatura", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nombre único de la asignatura")
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Schema(description = "Descripción opcional de la asignatura")
    @Size(max = 500)
    @Column(length = 500)
    private String description;
}
