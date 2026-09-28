package mathtexpedia.es.api.persistence.attemptAnswer;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class AttemptAnswerDao extends GenericJPADao implements AttemptAnswerDataService {

    @Override
    @Transactional(readOnly = true)
    public List<AttemptAnswer> getAttemptAnswersByAttempt(long attemptId) {
        logger.trace("Finding attempt answers by attempt id {}", attemptId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<AttemptAnswer> cq = cb.createQuery(AttemptAnswer.class);
        Root<AttemptAnswer> root = cq.from(AttemptAnswer.class);

        cq.select(root).where(cb.equal(root.get("attempt").get("id"), attemptId));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional
    public AttemptAnswer saveAttempt(AttemptAnswer attempt) {
        logger.trace("Saving attempt answer {}", attempt);

        em.persist(attempt);
        return attempt;
    }
}
