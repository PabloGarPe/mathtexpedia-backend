package mathtexpedia.es.api.domain.model.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.question.QuestionForAttemptDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizForAttemptDto {

    private Long id;
    private String name;
    private String description;
    private Difficulty difficulty;

    private List<QuestionForAttemptDto> questions;

}
