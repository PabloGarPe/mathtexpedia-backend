package mathtexpedia.es.api.service.userAccount;

import mathtexpedia.es.api.domain.model.userAccount.AuthorCandidateDto;
import mathtexpedia.es.api.domain.model.userAccount.AuthorDto;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.degree.DegreeMapper;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {

    private final DegreeMapper degreeMapper;

    public UserAccountMapper(DegreeMapper degreeMapper) {
        this.degreeMapper = degreeMapper;
    }

    public UserAccountDto toDto(UserAccount userAccount) {
        return new UserAccountDto(
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getUsername(),
                userAccount.getFirstName(),
                userAccount.getLastName(),
                userAccount.getBio(),
                userAccount.getUniversity(),
                userAccount.getStudies(),
                userAccount.getDegree() == null ? null : degreeMapper.toDto(userAccount.getDegree()),
                userAccount.getCurrentYear(),
                userAccount.hasAvatar(),
                userAccount.getAvatarUpdatedAt(),
                userAccount.getStatus(),
                userAccount.getCreatedAt(),
                userAccount.getLastSeenAt(),
                // Los usuarios que completaron el perfil antes de existir el username vuelven a contar como incompletos
                userAccount.getUsername() == null ? null : userAccount.getProfileCompletedAt()
        );
    }

    public AuthorCandidateDto toAuthorCandidateDto(UserAccount userAccount) {
        return new AuthorCandidateDto(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getFirstName(),
                userAccount.getLastName(),
                userAccount.getEmail(),
                userAccount.hasAvatar()
        );
    }

    public AuthorDto toAuthorDto(UserAccount userAccount) {
        return new AuthorDto(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.hasAvatar(),
                userAccount.getAvatarUpdatedAt()
        );
    }

    /**
     * Sustituye todos los datos del perfil: lo que no llega (o llega en blanco) se borra, salvo el username, que es obligatorio.
     * El grado llega ya resuelto y validado junto al curso (nulo = sin grado).
     */
    public void updateEntity(UserAccount target, UpdateUserAccountDto dto, Degree degree) {
        target.setUsername(dto.getUsername().trim());
        target.setFirstName(blankToNull(dto.getFirstName()));
        target.setLastName(blankToNull(dto.getLastName()));
        target.setBio(blankToNull(dto.getBio()));
        target.setUniversity(blankToNull(dto.getUniversity()));
        target.setStudies(blankToNull(dto.getStudies()));
        target.setDegree(degree);
        target.setCurrentYear(degree == null ? null : dto.getCurrentYear());
    }

    private static String blankToNull(String value) {
        if (value == null)
            return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
