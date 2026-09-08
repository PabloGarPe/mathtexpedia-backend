package mathtexpedia.es.api.domain.model.auth;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    public String oldPassword;
    public String newPassword;
}
