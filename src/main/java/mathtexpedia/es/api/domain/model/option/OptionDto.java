package mathtexpedia.es.api.domain.model.option;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptionDto {

    private Long id;
    private String text;
    private boolean isCorrect;
    private int position;
    private Long questionId;

}
