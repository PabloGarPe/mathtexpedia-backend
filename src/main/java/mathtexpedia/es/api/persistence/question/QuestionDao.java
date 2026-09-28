package mathtexpedia.es.api.persistence.question;

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
public class QuestionDao extends GenericJPADao implements QuestionDataService{
    @Override
    @Transactional(readOnly = true)
    public List<Question> getAllQuestionsByQuizId(Long quizId) {
        logger.trace("Getting all questions for quiz with id {}", quizId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Question> cq = cb.createQuery(Question.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(root).where(cb.equal(root.get("quiz").get("id"), quizId));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Long id) {
        logger.trace("Getting question with id {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Question> cq = cb.createQuery(Question.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(root).where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional
    public Question create(Question question) {
        logger.trace("Creating new question {}", question);
        em.persist(question);
        return question;
    }

    @Override
    @Transactional
    public Question update(Question question) {
        logger.trace("Updating question {}", question);
        return em.merge(question);
    }

    @Override
    @Transactional
    public void delete(Question question) {
        logger.trace("Deleting question {}", question);
        em.remove(em.contains(question) ? question : em.merge(question));
    }
}
