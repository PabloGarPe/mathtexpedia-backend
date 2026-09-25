package mathtexpedia.es.api.persistence.quizAttempt;

import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class QuizAttemptDao extends GenericJPADao implements QuizAttemptDataService {

    @Override
    @Transactional
    public QuizAttempt createQuizAttempt(QuizAttempt quizAttempt) {
        logger.trace("Creating quiz attempt: {}", quizAttempt);

        em.persist(quizAttempt);
        return quizAttempt;
    }
}
