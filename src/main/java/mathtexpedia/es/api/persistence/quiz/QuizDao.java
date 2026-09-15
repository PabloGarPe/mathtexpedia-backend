package mathtexpedia.es.api.persistence.quiz;

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
public class QuizDao extends GenericJPADao implements QuizDataService {

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getAll() {
        logger.trace("Getting all quizzes");

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root);

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> getById(long id) {
        logger.trace("Getting quiz with id {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getAllBySubjectId(long subjectId) {
        logger.trace("Getting all quizzes for subject with id {}", subjectId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(cb.equal(root.get("subject").get("id"), subjectId));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getAllBySubjectUnitId(long subjectUnitId) {
        logger.trace("Getting all quizzes for subject unit with id {}", subjectUnitId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(cb.equal(root.get("subjectUnit").get("id"), subjectUnitId));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional
    public Quiz create(Quiz quiz) {
        logger.trace("Creating new quiz {}", quiz);
        em.persist(quiz);
        return quiz;
    }

    @Override
    @Transactional
    public Quiz update(Quiz quiz) {
        logger.trace("Updating quiz {}", quiz);
        return em.merge(quiz);
    }

    @Override
    @Transactional
    public void delete(Quiz quiz) {
        logger.trace("Deleting quiz {}", quiz);
        em.remove(em.contains(quiz) ? quiz : em.merge(quiz));
    }
}
