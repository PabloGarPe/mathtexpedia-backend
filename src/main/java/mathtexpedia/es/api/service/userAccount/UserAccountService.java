package mathtexpedia.es.api.service.userAccount;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.AuthorCandidateDto;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.user.UserAccount;

import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public interface UserAccountService {

    UserAccount getOrProvision(UserProfile profile) throws MathtexpediaUnauthorizedException;

    UserAccountDto getUserAccount(UserProfile profile) throws MathtexpediaUnauthorizedException;

    UserAccountDto updateUserAccount(UserProfile profile, UpdateUserAccountDto dto) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException;

    /** Valida y normaliza la imagen y sustituye la foto de perfil actual. */
    UserAccountDto updateAvatar(UserProfile profile, MultipartFile file) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException;

    /** Quita la foto de perfil. No falla si no tenía. */
    UserAccountDto deleteAvatar(UserProfile profile) throws MathtexpediaUnauthorizedException;

    /** Cuándo se cambió la foto del usuario (sirve de ETag). Falla si el usuario no existe, está inactivo o no tiene foto. */
    Instant getAvatarVersion(long userId) throws MathtexpediaNotFoundException;

    /** JPEG de la foto del usuario. */
    byte[] getAvatarContent(long userId) throws MathtexpediaNotFoundException;

    List<AuthorCandidateDto> searchAuthorCandidates(String query, Integer limit);

    void deactivateUserAccount(UserProfile profile);
}
