package mathtexpedia.es.api.domain.model.auth;

import lombok.Data;

@Data
public class LoginDTO {

    private String email;
    private String password;
}
