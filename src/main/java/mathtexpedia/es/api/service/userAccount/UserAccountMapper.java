package mathtexpedia.es.api.service.userAccount;

import mathtexpedia.es.api.domain.model.userAccount.AuthorCandidateDto;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {

    public UserAccountDto toDto(UserAccount userAccount) {
        return new UserAccountDto(
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getName(),
                userAccount.getStatus(),
                userAccount.getCreatedAt(),
                userAccount.getLastSeenAt(),
                userAccount.getProfileCompletedAt()
        );
    }

    public AuthorCandidateDto toAuthorCandidateDto(UserAccount userAccount) {
        return new AuthorCandidateDto(
                userAccount.getId(),
                userAccount.getName(),
                userAccount.getEmail()
        );
    }

    public void updateEntity(UserAccount target, UpdateUserAccountDto dto) {
        target.setName(dto.getName());
    }
}
