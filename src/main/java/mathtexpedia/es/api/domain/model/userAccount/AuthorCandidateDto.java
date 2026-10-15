package mathtexpedia.es.api.domain.model.userAccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorCandidateDto {

    @Schema(description = "Identificador interno del usuario. Útil como clave en la lista y para excluir del lado del cliente a los ya seleccionados", example = "42")
    private Long id;

    @Schema(description = "Nombre del usuario. Nulo si aún no ha completado su perfil: en ese caso el front debe mostrar solo el email",
            nullable = true, example = "Ada Lovelace")
    private String name;

    @Schema(description = "Email del usuario. Es el valor que se envía en 'authorEmails' al crear o actualizar un PDF", example = "ada@mathtexpedia.es")
    private String email;
}
