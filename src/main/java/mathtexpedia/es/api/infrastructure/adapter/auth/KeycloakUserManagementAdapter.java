package mathtexpedia.es.api.infrastructure.adapter.auth;

import lombok.RequiredArgsConstructor;
import mathtexpedia.es.api.domain.exception.AuthenticationException;
import mathtexpedia.es.api.domain.model.auth.CreateUserRequest;
import mathtexpedia.es.api.domain.model.auth.ResetPasswordRequest;
import mathtexpedia.es.api.domain.model.auth.UserDTO;
import mathtexpedia.es.api.domain.model.mail.Mail;
import mathtexpedia.es.api.domain.port.auth.UserManagementPort;
import mathtexpedia.es.api.domain.port.mail.MailPort;
import mathtexpedia.es.api.domain.security.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class KeycloakUserManagementAdapter implements UserManagementPort {

    private final KeycloakAdminClient adminClient;
    private final KeycloakAdminTokenProvider tokenProvider;
    private final MailPort  mailPort;

    private static final Set<String> KNOWN_ROLES = Set.of("ROLE_ADMIN", "ROLE_USER");
    private static final String PASS = "password";
    private static final String CHANGE_IT= "change-it";
    public static final String OWN_EMAIL = "noreplay@mathtexpedia.es";
    public static final String SUBJECT = "Reset password";
    public static final String BODY_1 = "<!DOCTYPE html>\n" +
            "<html lang=\"es\">\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\">\n" +
            "  <title>Contraseña restablecida</title>\n" +
            "</head>\n" +
            "<body style=\"margin:0; padding:0; background-color:#f4f4f4; font-family:Arial, sans-serif;\">\n" +
            "  <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f4f4; padding:20px 0;\">\n" +
            "    <tr>\n" +
            "      <td align=\"center\">\n" +
            "        <table role=\"presentation\" width=\"500\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff; border-radius:8px; padding:30px; box-shadow:0 2px 6px rgba(0,0,0,0.1);\">\n" +
            "          <tr>\n" +
            "            <td style=\"text-align:center; padding-bottom:20px;\">\n" +
            "              <h2 style=\"color:#333333; margin:0;\">Restablecimiento de contraseña</h2>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "          <tr>\n" +
            "            <td style=\"color:#555555; font-size:15px; line-height:1.5;\">\n" +
            "              <p>Se ha resetado la contraseña correctamente.</p>\n" +
            "              <p>Para iniciar sesión use la contraseña:</p>\n" +
            "              <p style=\"text-align:center; margin:20px 0;\">\n" +
            "                <span style=\"display:inline-block; background-color:#f0f0f0; padding:10px 20px; border-radius:5px; font-size:18px; font-weight:bold; letter-spacing:1px; color:#222;\">";
    public static final String BODY_2 = "</span>\n" +
            "              </p>\n" +
            "              <p>Y luego podrás cambiarla.</p>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "        </table>\n" +
            "      </td>\n" +
            "    </tr>\n" +
            "  </table>\n" +
            "</body>\n" +
            "</html>";


    @Value("${app.auth.keycloak.realm}")
    private String realm;

    @Override
    public UserDTO createUser(CreateUserRequest command) {
        String token = tokenProvider.getAdminToken();

        KeycloakUserRepresentation user = KeycloakUserRepresentation.builder()
                .email(command.getEmail())
                .enabled(true)
                .emailVerified(false)
                .build();

        try {
            adminClient.createUser(realm, token, user);
        } catch (Exception e) {
            throw new AuthenticationException("Error al crear el usuario en el proveedor", e);
        }

        List<KeycloakUserRepresentation> found = adminClient.findByEmail(realm, token, command.getEmail());
        if (found.isEmpty()) {
            throw new AuthenticationException("Usuario creado pero no se pudo recuperar");
        }
        KeycloakUserRepresentation createdUser = found.getFirst();

        try {
            adminClient.resetPassword(realm, token, createdUser.getId(), CredentialRepresentation.builder().type(PASS)
                    .temporary(false).value(command.getPassword()).build());
        } catch (Exception e) {
            throw new AuthenticationException("Usuario creado pero no se pudo asignar contraseña", e);
        }

        return toUserDto(createdUser);
    }

    @Override
    public Optional<UserDTO> findById(String userId) {
        String token = tokenProvider.getAdminToken();
        try {
            KeycloakUserRepresentation user = adminClient.findById(realm, token, userId);
            return Optional.ofNullable(user).map(this::toUserDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void assignRole(String userId, String role) {
        String token = tokenProvider.getAdminToken();
        try {
            KeycloakRoleRepresentation roleRepresentation = adminClient.getRealmRole(realm, token, role);
            adminClient.assignRealmRoles(realm, token, userId, List.of(roleRepresentation));
        } catch (Exception e) {
            throw new AuthenticationException("Error al asignar el rol " + role, e);
        }
    }

    @Override
    public List<UserRole> getUserRoles(String userId) {
        String token = tokenProvider.getAdminToken();
        return adminClient.getUserRealmRoles(realm, token, userId).stream()
                .map(KeycloakRoleRepresentation::getName)
                .filter(name -> KNOWN_ROLES.contains(name.toUpperCase()))
                .map(UserRole::fromKeycloakRole)
                .distinct()
                .toList();
    }

    @Override
    public void changePassword(ResetPasswordRequest command) {
        String token = tokenProvider.getAdminToken();

        List<KeycloakUserRepresentation> found = adminClient.findByEmail(realm, token, command.getEmail());
        if (found.isEmpty()) {
            throw new AuthenticationException("Usuario no encontrado");
        }

        KeycloakUserRepresentation user = found.getFirst();

        try {
            adminClient.resetPassword(realm, token, user.getId(),
                    CredentialRepresentation.builder()
                            .type(PASS)
                            .temporary(false)
                            .value(command.getPassword())
                            .build());
        } catch (Exception e) {
            throw new AuthenticationException("No se pudo restablecer la contraseña", e);
        }

    }

    @Override
    public void sendResetPasswordEmail(ResetPasswordRequest command) {
        String token = tokenProvider.getAdminToken();

        List<KeycloakUserRepresentation> found = adminClient.findByEmail(realm, token, command.getEmail());
        if (found.isEmpty()) {
            throw new AuthenticationException("Usuario no encontrado");
        }

        KeycloakUserRepresentation user = found.getFirst();

        // TODO: En vez de esto, hacer una clase criptoutils que genere strings aleatorios
        CredentialRepresentation newCredential = CredentialRepresentation.builder()
                .type(PASS).temporary(false).value(CHANGE_IT).build();

        try {
            adminClient.resetPassword(realm, token, user.getId(),newCredential);
            Mail mail = Mail.builder().from(OWN_EMAIL).subject(SUBJECT).body(BODY_1 + CHANGE_IT + BODY_2).build();
            mailPort.sendMail(mail, command.getEmail());
        } catch (Exception e) {
            throw new AuthenticationException("No se pudo restablecer la contraseña", e);
        }

    }

    @Override
    public void deleteUser(String userEmail) {
        String token = tokenProvider.getAdminToken();

        List<KeycloakUserRepresentation> found = adminClient.findByEmail(realm, token, userEmail);
        if (found.isEmpty()) {
            return;
        }
        KeycloakUserRepresentation user = found.getFirst();
        try {
            adminClient.deleteUser(realm, token, user.getId());
        } catch (Exception e) {
            throw new AuthenticationException("No se pudo eliminar el usuario", e);
        }

    }

    @Override
    public List<UserDTO> findAllUsers() {
        String token = tokenProvider.getAdminToken();

        List<KeycloakUserRepresentation> found = adminClient.getAll(realm, token);
        List<UserDTO> users = new ArrayList<>();
        for (KeycloakUserRepresentation user : found) {
            users.add(toUserDto(user));
        }
        return users;
    }

    private UserDTO toUserDto(KeycloakUserRepresentation r) {
        return UserDTO.builder()
                .id(r.getId())
                .email(r.getEmail())
                .username(r.getUsername())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .enabled(r.isEnabled())
                .build();
    }
}