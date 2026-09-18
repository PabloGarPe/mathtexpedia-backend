package mathtexpedia.es.api.persistence.option;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class OptionDao extends GenericJPADao implements OptionDataService{

    @Override
    @Transactional(readOnly = true)
    public List<Option> getOptionsByQuestionId(long questionId) {
        logger.trace("Getting options for question with id {}", questionId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Option> cq = cb.createQuery(Option.class);
        Root<Option> root = cq.from(Option.class);

        cq.select(root).where(cb.equal(root.get("question").get("id"), questionId));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Option> getOptionById(long id) {
        logger.trace("Getting option with id {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Option> cq = cb.createQuery(Option.class);
        Root<Option> root = cq.from(Option.class);

        cq.select(root).where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional
    public Option create(Option option) {
        logger.trace("Creating new option {}", option);
        em.persist(option);
        return option;
    }

    @Override
    @Transactional
    public Option update(Option option) {
        logger.trace("Updating option {}", option);
        return em.merge(option);
    }

    @Override
    @Transactional
    public void delete(Option option) {
        logger.trace("Deleting option {}", option);
        em.remove(em.contains(option) ? option : em.merge(option));
    }
}
