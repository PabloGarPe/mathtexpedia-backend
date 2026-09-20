package mathtexpedia.es.api.persistence.user;

import java.util.Optional;

public interface UserAccountDataService {

    UserAccount create(UserAccount user);

    UserAccount update(UserAccount user);

    Optional<UserAccount> getByExternalId(String externalId);

}
