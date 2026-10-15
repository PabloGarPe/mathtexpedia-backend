package mathtexpedia.es.api.domain.model.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuizVisibilityDto {

    @Schema(description = "true para publicar el cuestionario (visible para los usuarios), false para ocultarlo", example = "true")
    @NotNull
    private Boolean visible;
}
