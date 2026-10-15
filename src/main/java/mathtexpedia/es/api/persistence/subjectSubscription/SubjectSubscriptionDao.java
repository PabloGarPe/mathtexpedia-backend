package mathtexpedia.es.api.persistence.subjectSubscription;

import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class SubjectSubscriptionDao extends GenericJPADao implements SubjectSubscriptionDataService {

    private static final String USER_PARAM = "userId";
    private static final String SUBJECT_PARAM = "subjectId";

    @Override
    @Transactional(readOnly = true)
    public List<SubjectSubscription> getByUser(long userAccountId) {
        logger.trace("Getting subscriptions of user {}", userAccountId);

        return em.createQuery("""
                        SELECT s FROM SubjectSubscription s JOIN FETCH s.subject sub
                        WHERE s.userAccount.id = :userId
                        ORDER BY sub.name, s.id
                        """, SubjectSubscription.class)
                .setParameter(USER_PARAM, userAccountId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubjectSubscription> getByUserAndSubject(long userAccountId, long subjectId) {
        logger.trace("Getting subscription of user {} to subject {}", userAccountId, subjectId);

        return em.createQuery("""
                        SELECT s FROM SubjectSubscription s JOIN FETCH s.subject
                        WHERE s.userAccount.id = :userId AND s.subject.id = :subjectId
                        """, SubjectSubscription.class)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(SUBJECT_PARAM, subjectId)
                .getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getSubjectIdsByUser(long userAccountId) {
        logger.trace("Getting subscribed subject ids of user {}", userAccountId);

        return new HashSet<>(em.createQuery(
                        "SELECT s.subject.id FROM SubjectSubscription s WHERE s.userAccount.id = :userId", Long.class)
                .setParameter(USER_PARAM, userAccountId)
                .getResultList());
    }

    @Override
    @Transactional
    public boolean add(long userAccountId, long subjectId) {
        logger.trace("Subscribing user {} to subject {}", userAccountId, subjectId);

        return em.createNativeQuery("""
                        INSERT INTO subject_subscription (user_id, subject_id, created_at)
                        VALUES (:userId, :subjectId, :now)
                        ON CONFLICT (user_id, subject_id) DO NOTHING
                        """)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(SUBJECT_PARAM, subjectId)
                .setParameter("now", Instant.now())
                .executeUpdate() > 0;
    }

    @Override
    @Transactional
    public boolean remove(long userAccountId, long subjectId) {
        logger.trace("Unsubscribing user {} from subject {}", userAccountId, subjectId);

        return em.createQuery("DELETE FROM SubjectSubscription s WHERE s.userAccount.id = :userId AND s.subject.id = :subjectId")
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(SUBJECT_PARAM, subjectId)
                .executeUpdate() > 0;
    }
}
