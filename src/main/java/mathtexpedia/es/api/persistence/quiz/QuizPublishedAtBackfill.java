package mathtexpedia.es.api.persistence.quiz;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Rellena {@code published_at} de los cuestionarios que ya eran visibles antes de existir la visibilidad. Sin esto,
 * ocultar uno antiguo y volver a publicarlo se tomaría por su primera publicación y notificaría a todos.
 * <p>
 * Es idempotente y seguro en cada arranque: el código siempre fija {@code published_at} al hacer visible un
 * cuestionario, así que solo puede afectar a filas anteriores a la migración.
 */
@Component
public class QuizPublishedAtBackfill implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(QuizPublishedAtBackfill.class);

    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate transactionTemplate;

    public QuizPublishedAtBackfill(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer updated = transactionTemplate.execute(status -> em.createNativeQuery("""
                UPDATE quiz
                SET published_at = COALESCE(last_time_edited, now())
                WHERE visible = true AND published_at IS NULL
                """).executeUpdate());

        if (updated != null && updated > 0)
            logger.info("Backfilled published_at for {} already visible quizzes", updated);
    }
}
