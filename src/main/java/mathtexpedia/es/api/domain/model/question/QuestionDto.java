package mathtexpedia.es.api.domain.model.question;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionDto {

    private Long id;
    private String text;
    private QuestionType type;
    private String explanation;
    private int position;
    private Long quizId;
}
