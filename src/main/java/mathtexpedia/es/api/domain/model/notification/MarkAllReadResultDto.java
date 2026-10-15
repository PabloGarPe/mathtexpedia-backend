package mathtexpedia.es.api.domain.model.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarkAllReadResultDto {

    @Schema(description = "Número de notificaciones que se han marcado como leídas", example = "5")
    private int updated;
}
