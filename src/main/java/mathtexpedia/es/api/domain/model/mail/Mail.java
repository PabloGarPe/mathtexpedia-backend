package mathtexpedia.es.api.domain.model.mail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mail {

    @Schema(description = "Remitente. Si no se indica, se usa el email del usuario autenticado")
    @Nullable
    String from;

    @Schema(description = "Asunto del correo")
    @NotBlank
    String subject;

    @Schema(description = "Cuerpo del correo")
    @NotBlank
    String body;

    @Schema(hidden = true, description = "No editable desde la API")
    @JsonIgnore
    String cc;

}
