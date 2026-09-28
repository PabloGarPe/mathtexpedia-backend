package mathtexpedia.es.api.domain.model.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.option.OptionDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionForCorrectionDto {

    private Long id;
    private String text;
    private QuestionType type;
    private String explanation;
    private int position;

    private List<OptionDto> options;
}
