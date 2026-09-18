package mathtexpedia.es.api.domain.model.option;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptionForAttemptDto {

    private Long id;
    private String text;
    private int position;

}
