package mathtexpedia.es.api.domain.model.userAccount;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserAccountDto {

    public static final String USERNAME_PATTERN = "^[A-Za-z0-9._-]{3,30}$";

    @Schema(description = "Nombre público que se muestra en la aplicación (apuntes, búsquedas…). Obligatorio, único sin distinguir "
            + "mayúsculas, de 3 a 30 caracteres entre letras sin tilde, números, punto, guion y guion bajo", example = "ada_lovelace")
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Pattern(regexp = USERNAME_PATTERN, message = "El nombre de usuario debe tener entre 3 y 30 caracteres: letras, números, '.', '-' o '_'")
    private String username;

    @Schema(description = "Nombre de pila (opcional). Nulo o vacío lo borra", nullable = true, example = "Ada")
    @Size(max = 100)
    private String firstName;

    @Schema(description = "Apellidos (opcional). Nulo o vacío lo borra", nullable = true, example = "Lovelace")
    @Size(max = 100)
    private String lastName;

    @Schema(description = "Biografía corta (opcional, máx. 280 caracteres). Nulo o vacío la borra", nullable = true)
    @Size(max = 280)
    private String bio;

    @Schema(description = "Universidad o centro de estudios (opcional). Nulo o vacío lo borra", nullable = true, example = "Universidad de Sevilla")
    @Size(max = 150)
    private String university;

    @Schema(description = "Estudios que cursa (opcional). Nulo o vacío lo borra", nullable = true, example = "Grado en Matemáticas")
    @Size(max = 150)
    private String studies;
}
