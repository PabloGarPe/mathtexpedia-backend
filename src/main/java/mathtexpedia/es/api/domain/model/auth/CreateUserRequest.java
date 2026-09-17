package mathtexpedia.es.api.domain.model.auth;


import lombok.Data;

@Data
public class CreateUserRequest {
        private final String email;
        private final String password;

}
