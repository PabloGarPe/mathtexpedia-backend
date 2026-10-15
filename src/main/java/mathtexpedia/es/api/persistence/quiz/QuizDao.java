package mathtexpedia.es.api.persistence.quiz;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class QuizDao extends GenericJPADao implements QuizDataService {

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getAll(boolean visibleOnly) {
        logger.trace("Getting all quizzes");

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(visibilityPredicate(cb, root, visibleOnly));

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
    public List<Quiz> getAllBySubjectId(long subjectId, boolean visibleOnly) {
        logger.trace("Getting all quizzes for subject with id {}", subjectId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(cb.equal(root.get("subject").get("id"), subjectId), visibilityPredicate(cb, root, visibleOnly));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getAllBySubjectUnitId(long subjectUnitId, boolean visibleOnly) {
        logger.trace("Getting all quizzes for subject unit with id {}", subjectUnitId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(cb.equal(root.get("subjectUnit").get("id"), subjectUnitId), visibilityPredicate(cb, root, visibleOnly));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getRecent(int limit, boolean visibleOnly) {
        logger.trace("Getting {} most recent quizzes", limit);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(cb.isNotNull(root.get("lastTimeEdited")), visibilityPredicate(cb, root, visibleOnly))
                .orderBy(cb.desc(root.get("lastTimeEdited")), cb.desc(root.get("id")));

        return session.createQuery(cq).setMaxResults(limit).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getByIds(Collection<Long> quizIds) {
        if (quizIds == null || quizIds.isEmpty())
            return List.of();

        logger.trace("Getting quizzes with ids {}", quizIds);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
        Root<Quiz> root = cq.from(Quiz.class);

        cq.select(root).where(root.get("id").in(quizIds));

        return session.createQuery(cq).getResultList();
    }

    /** Con {@code visibleOnly} false devuelve una conjunción vacía (no filtra). */
    private static Predicate visibilityPredicate(CriteriaBuilder cb, Root<Quiz> root, boolean visibleOnly) {
        return visibleOnly ? cb.isTrue(root.get("visible")) : cb.conjunction();
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
