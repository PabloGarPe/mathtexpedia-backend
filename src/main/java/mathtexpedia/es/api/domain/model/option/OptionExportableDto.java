package mathtexpedia.es.api.domain.model.option;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptionExportableDto {

    @NotNull
    @NotBlank
    private String text;

    private boolean isCorrect;
    private int position;

}
