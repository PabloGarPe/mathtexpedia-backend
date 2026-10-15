package mathtexpedia.es.api.service.userAccount;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.AuthorCandidateDto;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserStatus;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.user.UserAccountDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountServiceImpl.class);

    private static final int MIN_SEARCH_LENGTH = 2;
    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MAX_SEARCH_LIMIT = 20;

    private final UserAccountDataService userAccountDataService;
    private final UserAccountMapper userAccountMapper;

    public UserAccountServiceImpl(UserAccountDataService userAccountDataService, UserAccountMapper userAccountMapper) {
        this.userAccountDataService = userAccountDataService;
        this.userAccountMapper = userAccountMapper;
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
    public UserAccountDto updateUserAccount(UserProfile profile, UpdateUserAccountDto dto) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException {
        logger.info("Updating user account for external ID: {}", profile.getId());

        UserAccount toUpdate = getOrProvision(profile);

        userAccountMapper.updateEntity(toUpdate, dto);

        if (toUpdate.getProfileCompletedAt() == null) {
            toUpdate.setProfileCompletedAt(Instant.now());
        }

        try {
            UserAccount updatedUserAccount = userAccountDataService.update(toUpdate);
            return userAccountMapper.toDto(updatedUserAccount);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating user account for external ID: " + profile.getId(), e);
        }
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
                    account.setName(null);
                    userAccountDataService.update(account);
                });
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
            return userAccountDataService.create(account);
        } catch (DataIntegrityViolationException e) {
            return userAccountDataService.getByExternalId(profile.getId()).orElseThrow();
        }
    }
}
