package mathtexpedia.es.api.domain.model.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mathtexpedia.es.api.domain.model.question.QuestionExportableDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportQuestionsDto {

    @Schema(description = "Preguntas a añadir al final del cuestionario, en el orden de la lista. Se ignora la position "
            + "de cada pregunta y de cada opción: se numeran según su orden en la lista")
    @Valid
    @NotEmpty
    private List<QuestionExportableDto> questions;
}
