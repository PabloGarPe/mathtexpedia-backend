package mathtexpedia.es.api.service.userAccount;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.user.UserAccount;

public interface UserAccountService {

    UserAccount getOrProvision(UserProfile profile) throws MathtexpediaUnauthorizedException;

    UserAccountDto getUserAccount(UserProfile profile) throws MathtexpediaUnauthorizedException;

    UserAccountDto updateUserAccount(UserProfile profile, UpdateUserAccountDto dto) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException;

    void deactivateUserAccount(UserProfile profile);
}
