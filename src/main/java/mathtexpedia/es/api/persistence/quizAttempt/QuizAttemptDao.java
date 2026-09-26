package mathtexpedia.es.api.persistence.quizAttempt;

import jakarta.persistence.TypedQuery;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class QuizAttemptDao extends GenericJPADao implements QuizAttemptDataService {

    private static final String USER_ID_PARAM = "userId";

    private static final String FIND_BY_USER_ID_ASC =
            "SELECT a FROM QuizAttempt a WHERE a.user.id = :userId ORDER BY a.submittedAt ASC";
    private static final String FIND_BY_USER_ID_DESC =
            "SELECT a FROM QuizAttempt a WHERE a.user.id = :userId ORDER BY a.submittedAt DESC";

    @Override
    @Transactional
    public QuizAttempt createQuizAttempt(QuizAttempt quizAttempt) {
        logger.trace("Creating quiz attempt: {}", quizAttempt);

        em.persist(quizAttempt);
        return quizAttempt;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttempt> getByUserId(long userId, Pageable pageable) {
        logger.trace("Getting quiz attempts for user: {}", userId);

        Sort.Order submittedAtOrder = pageable.getSort().getOrderFor("submittedAt");
        boolean ascending = submittedAtOrder != null && submittedAtOrder.isAscending();

        TypedQuery<QuizAttempt> query = em.createQuery(
                ascending ? FIND_BY_USER_ID_ASC : FIND_BY_USER_ID_DESC,
                QuizAttempt.class
        );
        query.setParameter(USER_ID_PARAM, userId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = em.createQuery("SELECT COUNT(a) FROM QuizAttempt a WHERE a.user.id = :userId", Long.class)
                .setParameter(USER_ID_PARAM, userId)
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttempt> getByQuizIdAndUserId(long quizId, long userId) {
        logger.trace("Getting quiz attempts for quiz: {} and user: {}", quizId, userId);

        TypedQuery<QuizAttempt> query = em.createQuery(
                "SELECT a FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.user.id = :userId ORDER BY a.submittedAt DESC",
                QuizAttempt.class
        );
        query.setParameter("quizId", quizId);
        query.setParameter(USER_ID_PARAM, userId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptStats getStatsByUserId(long userId) {
        logger.trace("Getting quiz attempt stats for user: {}", userId);

        TypedQuery<QuizAttemptStats> query = em.createQuery(
                "SELECT new mathtexpedia.es.api.persistence.quizAttempt.QuizAttemptStats(" +
                        "COUNT(a), " +
                        "SUM(CASE WHEN a.unansweredQuestions = 0 THEN 1 ELSE 0 END), " +
                        "SUM(a.correctAnswers), " +
                        "SUM(a.totalQuestions - a.unansweredQuestions), " +
                        "AVG(a.score), " +
                        "MAX(a.score)) " +
                        "FROM QuizAttempt a WHERE a.user.id = :userId",
                QuizAttemptStats.class
        );
        query.setParameter(USER_ID_PARAM, userId);
        return query.getSingleResult();
    }
}
