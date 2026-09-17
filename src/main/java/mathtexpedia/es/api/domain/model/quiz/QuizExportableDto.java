package mathtexpedia.es.api.domain.model.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.question.QuestionExportableDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizExportableDto {

    @NotNull
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Difficulty difficulty;

    @Valid
    @NotEmpty
    private List<QuestionExportableDto> questions;

}
