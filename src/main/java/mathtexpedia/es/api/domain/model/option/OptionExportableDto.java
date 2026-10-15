package mathtexpedia.es.api.domain.model.option;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OptionExportableDto {

    @NotNull
    @NotBlank
    private String text;

    private boolean isCorrect;
    private int position;

    // Lombok nombra el getter isCorrect() (propiedad "correct"), pero el parámetro del constructor se llamaría
    // "isCorrect": el export escribía "correct" y el import esperaba "isCorrect", y exportar → importar fallaba.
    // La propiedad canónica es "correct" (también en el OpenAPI); "isCorrect" se sigue aceptando al importar.
    @JsonCreator
    public OptionExportableDto(@JsonProperty("text") String text,
                               @JsonProperty("correct") @JsonAlias("isCorrect") boolean isCorrect,
                               @JsonProperty("position") int position) {
        this.text = text;
        this.isCorrect = isCorrect;
        this.position = position;
    }
}
