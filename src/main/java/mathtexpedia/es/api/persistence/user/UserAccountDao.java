package mathtexpedia.es.api.persistence.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserAccountDao extends GenericJPADao implements UserAccountDataService {


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
}
