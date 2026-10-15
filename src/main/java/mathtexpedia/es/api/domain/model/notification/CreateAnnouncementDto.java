package mathtexpedia.es.api.domain.model.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateAnnouncementDto {

    @Schema(description = "Título del aviso", example = "Mantenimiento programado", maxLength = 120)
    @NotBlank
    @Size(max = 120)
    private String title;

    @Schema(description = "Texto del aviso", example = "El sábado la plataforma estará en mantenimiento de 2:00 a 4:00.", maxLength = 500)
    @NotBlank
    @Size(max = 500)
    private String message;

    @Schema(description = "Ruta interna del front a la que llevará el aviso (debe empezar por '/'). Opcional",
            example = "/dashboard", maxLength = 300, nullable = true)
    @Size(max = 300)
    private String link;

    @Schema(description = "Emails de los destinatarios. Si no se indica o está vacío, se envía a todos los usuarios activos",
            nullable = true)
    private List<@Email String> recipientEmails;
}
