package mathtexpedia.es.api.persistence.subject;

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
public class SubjectDao extends GenericJPADao implements SubjectDataService {

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getAll() {
        logger.trace("Getting all subjects");

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Subject> cq = cb.createQuery(Subject.class);
        Root<Subject> root = cq.from(Subject.class);

        cq.select(root);

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subject> getById(long id) {
        logger.trace("Getting subject with id {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Subject> cq = cb.createQuery(Subject.class);
        Root<Subject> root = cq.from(Subject.class);

        cq.select(root).where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional
    public Subject create(Subject subject) {
        logger.trace("Creating new subject {}", subject);
        em.persist(subject);
        return subject;
    }

    @Override
    @Transactional
    public Subject update(Subject subject) {
        logger.trace("Updating subject {}", subject);
        return em.merge(subject);
    }

    @Override
    @Transactional
    public void delete(Subject subject) {
        logger.trace("Deleting subject {}", subject);
        em.remove(em.contains(subject) ? subject : em.merge(subject));
    }
}
