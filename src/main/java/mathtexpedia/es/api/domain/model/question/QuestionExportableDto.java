package mathtexpedia.es.api.domain.model.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.option.OptionExportableDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionExportableDto {

    @NotNull
    @NotBlank
    private String text;

    private int position;

    @NotNull
    private QuestionType type;

    private String explanation;

    @Valid
    @NotEmpty
    private List<OptionExportableDto> options;
}
