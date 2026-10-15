package mathtexpedia.es.api.persistence.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.domain.model.userAccount.UserStatus;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class UserAccountDao extends GenericJPADao implements UserAccountDataService {

    private static final char LIKE_ESCAPE = '\\';

    @Override
    @Transactional
    public UserAccount create(UserAccount user) {
        logger.trace("Creating user account {}", user);

        em.persist(user);
        return user;
    }

    @Override
    @Transactional
    public UserAccount update(UserAccount user) {
        logger.trace("Updating user account {}", user);

        return em.merge(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> getByExternalId(String externalId) {
        logger.trace("Finding user account by external id {}", externalId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> root = cq.from(UserAccount.class);

        cq.select(root).where(cb.equal(root.get("externalId"), externalId));

        return session.createQuery(cq).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccount> getByEmails(Collection<String> emails) {
        logger.trace("Finding user accounts by emails {}", emails);

        if (emails.isEmpty())
            return List.of();

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> root = cq.from(UserAccount.class);

        cq.select(root).where(cb.lower(root.get("email")).in(emails));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccount> searchActive(String term, int limit) {
        logger.trace("Searching active user accounts by term '{}' (limit: {})", term, limit);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> root = cq.from(UserAccount.class);

        String pattern = "%" + escapeLike(term.toLowerCase(Locale.ROOT)) + "%";
        Expression<String> email = cb.lower(root.get("email"));
        Expression<String> name = cb.lower(root.get("name"));

        cq.select(root)
                .where(
                        cb.equal(root.get("status"), UserStatus.ACTIVE),
                        cb.isNotNull(root.get("email")),
                        cb.or(
                                cb.like(email, pattern, LIKE_ESCAPE),
                                cb.like(name, pattern, LIKE_ESCAPE)
                        )
                )
                .orderBy(cb.asc(cb.coalesce(name, email)), cb.asc(root.get("id")));

        return session.createQuery(cq).setMaxResults(limit).getResultList();
    }

    /** Evita que '%', '_' o '\\' escritos por el usuario actúen como comodines. */
    private static String escapeLike(String term) {
        return term
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
