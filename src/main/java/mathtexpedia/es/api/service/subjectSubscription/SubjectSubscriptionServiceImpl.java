package mathtexpedia.es.api.service.subjectSubscription;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subjectSubscription.SubjectSubscriptionDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectSubscription.SubjectSubscription;
import mathtexpedia.es.api.persistence.subjectSubscription.SubjectSubscriptionDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.subject.SubjectMapper;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectSubscriptionServiceImpl implements SubjectSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectSubscriptionServiceImpl.class);

    private final SubjectSubscriptionDataService subscriptionDataService;
    private final SubjectDataService subjectDataService;
    private final UserAccountService userAccountService;
    private final SubjectMapper subjectMapper;

    public SubjectSubscriptionServiceImpl(SubjectSubscriptionDataService subscriptionDataService,
                                          SubjectDataService subjectDataService,
                                          UserAccountService userAccountService,
                                          SubjectMapper subjectMapper) {
        this.subscriptionDataService = subscriptionDataService;
        this.subjectDataService = subjectDataService;
        this.userAccountService = userAccountService;
        this.subjectMapper = subjectMapper;
    }

    @Override
    public List<SubjectSubscriptionDto> getMySubscriptions(UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.info("Fetching subscriptions of user {}", user.getId());

        UserAccount account = userAccountService.getOrProvision(user);
        return subscriptionDataService.getByUser(account.getId()).stream().map(this::toDto).toList();
    }

    @Override
    public SubjectSubscriptionDto subscribe(UserProfile user, long subjectId)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.info("User {} subscribing to subject {}", user.getId(), subjectId);

        UserAccount account = userAccountService.getOrProvision(user);
        if (subjectDataService.getById(subjectId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);

        try {
            subscriptionDataService.add(account.getId(), subjectId);
        } catch (PersistenceException e) {
            // La asignatura se ha borrado justo entre la comprobación y el alta: la FK lo impide
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);
        }

        return subscriptionDataService.getByUserAndSubject(account.getId(), subjectId)
                .map(this::toDto)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + subjectId));
    }

    @Override
    public void unsubscribe(UserProfile user, long subjectId) throws MathtexpediaUnauthorizedException {
        logger.info("User {} unsubscribing from subject {}", user.getId(), subjectId);

        UserAccount account = userAccountService.getOrProvision(user);
        subscriptionDataService.remove(account.getId(), subjectId);
    }

    private SubjectSubscriptionDto toDto(SubjectSubscription subscription) {
        return new SubjectSubscriptionDto(subjectMapper.toDto(subscription.getSubject()), subscription.getCreatedAt());
    }
}
