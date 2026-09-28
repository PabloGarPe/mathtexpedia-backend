package mathtexpedia.es.api.domain.model.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.question.QuestionForCorrectionDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizForCorrectionDto {

    private Long id;
    private String name;
    private String description;
    private Difficulty difficulty;

    private List<QuestionForCorrectionDto> questions;
}
