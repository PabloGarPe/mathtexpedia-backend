package mathtexpedia.es.api.domain.model.userAccount;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserAccountDto {

    @Schema(description = "Nombre del usuario", example = "Pedro1234")
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
}
