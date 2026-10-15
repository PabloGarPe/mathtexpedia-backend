package mathtexpedia.es.api.persistence.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountDataService {

    UserAccount create(UserAccount user);

    UserAccount update(UserAccount user);

    Optional<UserAccount> getByExternalId(String externalId);

    List<UserAccount> getByEmails(Collection<String> emails);

    Optional<UserAccount> getById(long id);

    /** Si otro usuario (distinto de {@code excludingId}) ya tiene ese username, sin distinguir mayúsculas. */
    boolean usernameTaken(String username, long excludingId);

    /**
     * Usuarios ACTIVE cuyo username, nombre, apellidos o email contiene el término (sin distinguir mayúsculas ni tildes),
     * ordenados por username (o email si no tienen). Requiere la extensión 'unaccent' de Postgres.
     */
    List<UserAccount> searchActive(String term, int limit);

}
