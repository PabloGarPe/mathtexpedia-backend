package mathtexpedia.es.api.persistence.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountDataService {

    UserAccount create(UserAccount user);

    UserAccount update(UserAccount user);

    Optional<UserAccount> getByExternalId(String externalId);

    List<UserAccount> getByEmails(Collection<String> emails);

    /**
     * Usuarios ACTIVE cuyo nombre o email contiene el término (sin distinguir mayúsculas ni tildes), ordenados por nombre
     * (o email si no tienen nombre). Requiere la extensión 'unaccent' de Postgres.
     */
    List<UserAccount> searchActive(String term, int limit);

}
