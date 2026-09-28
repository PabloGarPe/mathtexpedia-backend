package mathtexpedia.es.api.domain.model.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

        @Email
        @NotBlank
        private final String email;

        @Size(min = 8, message = "Password must be at least 8 characters long")
        private final String password;

}
