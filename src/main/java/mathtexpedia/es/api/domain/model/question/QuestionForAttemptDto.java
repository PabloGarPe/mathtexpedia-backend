package mathtexpedia.es.api.domain.model.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.option.OptionForAttemptDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionForAttemptDto {

    private Long id;
    private String text;
    private QuestionType type;
    private int position;

    private List<OptionForAttemptDto> options;

}
