package mathtexpedia.es.api.service.userAccount;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.event.UserProvisionedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.AuthorCandidateDto;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserStatus;
import mathtexpedia.es.api.domain.port.avatar.AvatarStoragePort;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.degree.DegreeDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.user.UserAccountDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountServiceImpl.class);

    private static final String AVATAR_KEY_PREFIX = "avatars/";
    private static final int MIN_SEARCH_LENGTH = 2;
    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MAX_SEARCH_LIMIT = 20;

    private final UserAccountDataService userAccountDataService;
    private final DegreeDataService degreeDataService;
    private final UserAccountMapper userAccountMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AvatarStoragePort avatarStoragePort;
    private final AvatarImageProcessor avatarImageProcessor;

    public UserAccountServiceImpl(UserAccountDataService userAccountDataService, DegreeDataService degreeDataService,
                                  UserAccountMapper userAccountMapper,
                                  ApplicationEventPublisher eventPublisher, AvatarStoragePort avatarStoragePort,
                                  AvatarImageProcessor avatarImageProcessor) {
        this.userAccountDataService = userAccountDataService;
        this.degreeDataService = degreeDataService;
        this.userAccountMapper = userAccountMapper;
        this.eventPublisher = eventPublisher;
        this.avatarStoragePort = avatarStoragePort;
        this.avatarImageProcessor = avatarImageProcessor;
    }

    @Override
    public UserAccount getOrProvision(UserProfile profile) throws MathtexpediaUnauthorizedException {
        logger.info("Getting or provisioning user account for external ID: {}", profile.getId());

        Optional<UserAccount> existing = userAccountDataService.getByExternalId(profile.getId());
        if (existing.isEmpty()) {
            return provision(profile);
        }

        UserAccount account = existing.get();
        if (account.getStatus() != UserStatus.ACTIVE) {
            throw new MathtexpediaUnauthorizedException("User account is not active: " + profile.getId());
        }

        return touch(account, profile);
    }

    @Override
    public UserAccountDto getUserAccount(UserProfile profile) throws MathtexpediaUnauthorizedException {
        logger.info("Getting user account for external ID: {}", profile.getId());

        return userAccountMapper.toDto(getOrProvision(profile));
    }

    @Override
    public UserAccountDto updateUserAccount(UserProfile profile, UpdateUserAccountDto dto)
            throws MathtexpediaConflictException, MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Updating user account for external ID: {}", profile.getId());

        UserAccount toUpdate = getOrProvision(profile);

        String username = dto.getUsername().trim();
        if (userAccountDataService.usernameTaken(username, toUpdate.getId()))
            throw new MathtexpediaConflictException("Username already in use: " + username);

        Degree degree = resolveDegree(dto);

        // Sin username el perfil no cuenta como completo, aunque tenga fecha de antes de que existiera el username
        boolean wasIncomplete = toUpdate.getUsername() == null;

        userAccountMapper.updateEntity(toUpdate, dto, degree);

        if (wasIncomplete || toUpdate.getProfileCompletedAt() == null) {
            toUpdate.setProfileCompletedAt(Instant.now());
        }

        try {
            UserAccount updatedUserAccount = userAccountDataService.update(toUpdate);
            return userAccountMapper.toDto(updatedUserAccount);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // También cubre la carrera con otra petición que haya tomado el mismo username entre la comprobación y el guardado
            throw new MathtexpediaConflictException("Error updating user account for external ID: " + profile.getId(), e);
        }
    }

    @Override
    public UserAccountDto updateAvatar(UserProfile profile, MultipartFile file) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        logger.info("Updating avatar for external ID: {}", profile.getId());

        if (file == null || file.isEmpty())
            throw new MathtexpediaInvalidException("Avatar image is required");

        byte[] normalized;
        try {
            normalized = avatarImageProcessor.normalize(file.getBytes());
        } catch (IOException e) {
            throw new MathtexpediaInvalidException("Uploaded file could not be read");
        }

        UserAccount account = getOrProvision(profile);

        // Primero el objeto y después la fila: si falla la base de datos solo queda un objeto que el siguiente cambio sobrescribe
        avatarStoragePort.upload(avatarKey(account.getId()), normalized, AvatarImageProcessor.OUTPUT_CONTENT_TYPE);

        account.setAvatarUpdatedAt(Instant.now());
        return userAccountMapper.toDto(userAccountDataService.update(account));
    }

    @Override
    public UserAccountDto deleteAvatar(UserProfile profile) throws MathtexpediaUnauthorizedException {
        logger.info("Deleting avatar for external ID: {}", profile.getId());

        UserAccount account = getOrProvision(profile);

        if (account.hasAvatar()) {
            account.setAvatarUpdatedAt(null);
            account = userAccountDataService.update(account);
            safeDeleteAvatar(account.getId());
        }
        return userAccountMapper.toDto(account);
    }

    @Override
    public Instant getAvatarVersion(long userId) throws MathtexpediaNotFoundException {
        return userAccountDataService.getById(userId)
                .filter(account -> account.getStatus() == UserStatus.ACTIVE)
                .map(UserAccount::getAvatarUpdatedAt)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Avatar not found for user " + userId));
    }

    @Override
    public byte[] getAvatarContent(long userId) throws MathtexpediaNotFoundException {
        getAvatarVersion(userId);

        return avatarStoragePort.download(avatarKey(userId)).orElseThrow(() -> {
            logger.error("Avatar of user {} exists in DB but not in S3", userId);
            return new MathtexpediaNotFoundException("Avatar content not found for user " + userId);
        });
    }

    @Override
    public List<AuthorCandidateDto> searchAuthorCandidates(String query, Integer limit) {
        logger.info("Searching author candidates (query length: {}, limit: {})", query == null ? 0 : query.length(), limit);

        String term = query == null ? "" : query.trim();
        if (term.length() < MIN_SEARCH_LENGTH)
            return List.of();

        int normalizedLimit = limit == null ? DEFAULT_SEARCH_LIMIT : Math.max(1, Math.min(limit, MAX_SEARCH_LIMIT));

        return userAccountDataService.searchActive(term, normalizedLimit).stream()
                .map(userAccountMapper::toAuthorCandidateDto)
                .toList();
    }

    @Override
    public void deactivateUserAccount(UserProfile profile) {
        logger.info("Deactivating user account for external ID: {}", profile.getId());

        userAccountDataService.getByExternalId(profile.getId())
                .ifPresent(account -> {
                    account.setStatus(UserStatus.INACTIVE);
                    account.setEmail(null);
                    account.setUsername(null);
                    account.setFirstName(null);
                    account.setLastName(null);
                    account.setBio(null);
                    account.setUniversity(null);
                    account.setStudies(null);
                    account.setDegree(null);
                    account.setCurrentYear(null);
                    boolean hadAvatar = account.hasAvatar();
                    account.setAvatarUpdatedAt(null);
                    userAccountDataService.update(account);
                    if (hadAvatar)
                        safeDeleteAvatar(account.getId());
                });
    }

    /** Resuelve y valida el grado y el curso del perfil: curso sin grado es 400, grado inexistente 404 y curso fuera de sus años 400. */
    private Degree resolveDegree(UpdateUserAccountDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        if (dto.getDegreeId() == null) {
            if (dto.getCurrentYear() != null)
                throw new MathtexpediaInvalidException("currentYear requires a degreeId");
            return null;
        }

        Degree degree = degreeDataService.getById(dto.getDegreeId())
                .orElseThrow(() -> new MathtexpediaNotFoundException("Degree not found with id: " + dto.getDegreeId()));
        if (dto.getCurrentYear() != null && dto.getCurrentYear() > degree.getYears())
            throw new MathtexpediaInvalidException("Current year " + dto.getCurrentYear() + " is out of range for degree "
                    + degree.getId() + " (1.." + degree.getYears() + ")");
        return degree;
    }

    private static String avatarKey(long userId) {
        return AVATAR_KEY_PREFIX + userId + ".jpg";
    }

    /** El borrado en S3 es de limpieza: si falla no se debe romper la operación, solo dejar rastro. */
    private void safeDeleteAvatar(long userId) {
        try {
            avatarStoragePort.delete(avatarKey(userId));
        } catch (RuntimeException e) {
            logger.warn("Could not delete avatar object of user {}", userId, e);
        }
    }

    private UserAccount touch(UserAccount account, UserProfile profile) {
        account.setEmail(profile.getEmail());
        account.setLastSeenAt(Instant.now());
        return userAccountDataService.update(account);
    }

    private UserAccount provision(UserProfile profile) {
        UserAccount account = new UserAccount();
        account.setExternalId(profile.getId());
        account.setEmail(profile.getEmail());
        account.setCreatedAt(Instant.now());
        account.setLastSeenAt(Instant.now());

        try {
            UserAccount created = userAccountDataService.create(account);
            // Solo si realmente se creó aquí (no en la carrera con otra petición del catch)
            eventPublisher.publishEvent(new UserProvisionedEvent(created.getId()));
            return created;
        } catch (DataIntegrityViolationException e) {
            return userAccountDataService.getByExternalId(profile.getId()).orElseThrow();
        }
    }
}
