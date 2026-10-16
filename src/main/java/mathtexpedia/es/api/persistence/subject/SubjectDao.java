package mathtexpedia.es.api.persistence.subject;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public List<Subject> getFiltered(Long degreeId, Integer year, Integer semester, boolean unassigned) {
        logger.trace("Getting subjects filtered by degree {}, year {}, semester {}, unassigned {}", degreeId, year, semester, unassigned);

        // La consulta se construye según haga falta en vez de pasar parámetros nulos (Postgres no infiere su tipo)
        StringBuilder jpql = new StringBuilder("SELECT s FROM Subject s");
        List<String> conditions = new ArrayList<>();
        if (unassigned) {
            conditions.add("NOT EXISTS (SELECT 1 FROM DegreeSubject ds WHERE ds.subject = s)");
        } else if (degreeId != null) {
            StringBuilder exists = new StringBuilder("EXISTS (SELECT 1 FROM DegreeSubject ds WHERE ds.subject = s AND ds.degree.id = :degreeId");
            if (year != null)
                exists.append(" AND ds.year = :year");
            if (semester != null)
                exists.append(" AND ds.semester = :semester");
            conditions.add(exists.append(")").toString());
        }
        if (!conditions.isEmpty())
            jpql.append(" WHERE ").append(String.join(" AND ", conditions));
        jpql.append(" ORDER BY s.name");

        TypedQuery<Subject> query = em.createQuery(jpql.toString(), Subject.class);
        if (!unassigned && degreeId != null) {
            query.setParameter("degreeId", degreeId);
            if (year != null)
                query.setParameter("year", year);
            if (semester != null)
                query.setParameter("semester", semester);
        }

        return query.getResultList();
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
        Subject merged = em.merge(subject);
        // Para que la violación de unicidad del nombre salte aquí y no al hacer commit fuera del DAO
        em.flush();
        return merged;
    }

    @Override
    @Transactional
    public void delete(Subject subject) {
        logger.trace("Deleting subject {}", subject);
        em.remove(em.contains(subject) ? subject : em.merge(subject));
    }
}
