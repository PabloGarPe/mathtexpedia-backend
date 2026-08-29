package mathtexpedia.es.api.persistence.subjectUnit;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class SubjectUnitDao extends GenericJPADao implements SubjectUnitDataService {
    @Override
    @Transactional(readOnly = true)
    public List<SubjectUnit> getAllForSubject(long subjectId) {
        logger.trace("Getting all subject units for subject with id {}", subjectId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<SubjectUnit> cq = cb.createQuery(SubjectUnit.class);
        Root<SubjectUnit> root = cq.from(SubjectUnit.class);

        cq.select(root).where(cb.equal(root.get("subject").get("id"), subjectId));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubjectUnit> getById(long id) {
        logger.trace("Getting subject unit with id {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<SubjectUnit> cq = cb.createQuery(SubjectUnit.class);
        Root<SubjectUnit> root = cq.from(SubjectUnit.class);

        cq.select(root).where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional
    public SubjectUnit create(SubjectUnit subjectUnit) {
        logger.trace("Creating new subject unit {}", subjectUnit);
        em.persist(subjectUnit);
        return subjectUnit;
    }

    @Override
    @Transactional
    public SubjectUnit update(SubjectUnit subjectUnit) {
        logger.trace("Updating subject unit {}", subjectUnit);
        return em.merge(subjectUnit);
    }

    @Override
    @Transactional
    public void delete(SubjectUnit subjectUnit) {
        logger.trace("Deleting subject unit {}", subjectUnit);
        em.remove(em.contains(subjectUnit) ? subjectUnit : em.merge(subjectUnit));
    }
}
