package mathtexpedia.es.api.domain.model.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;

import java.time.Instant;

@Data
@AllArgsConstructor
public class QuizDto {

    @Schema(description = "Identificador interno del cuestionario", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del cuestionario")
    private String name;

    @Schema(description = "Descripción opcional del cuestionario")
    private String description;

    @Schema(description = "Dificultad del cuestionario")
    private Difficulty difficulty;

    @Schema(description = "Fecha de última edición del cuestionario", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant lastTimeEdited;

    @Schema(description = "Asignatura a la que pertenece el cuestionario")
    private SubjectDto subject;

    @Schema(description = "Tema al que pertenece el cuestionario", nullable = true)
    private SubjectUnitDto subjectUnit;

    @Schema(description = "Si el cuestionario es visible para los usuarios. Los ocultos solo los ve un administrador")
    private boolean visible;

    @Schema(description = "Fecha de la primera publicación (primera vez que se hizo visible). Nula si nunca se ha publicado",
            nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private Instant publishedAt;

    @Schema(description = "Número total de veces que se ha hecho el cuestionario (todos los usuarios; cada envío cuenta, aunque sea del mismo usuario). 0 si nadie lo ha hecho",
            example = "128", accessMode = Schema.AccessMode.READ_ONLY)
    private long attemptCount;
}
