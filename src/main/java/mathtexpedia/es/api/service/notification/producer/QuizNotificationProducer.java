package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.QuizPublishedEvent;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class QuizNotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(QuizNotificationProducer.class);

    private final QuizDataService quizDataService;
    private final NotificationDispatcher dispatcher;

    public QuizNotificationProducer(QuizDataService quizDataService, NotificationDispatcher dispatcher) {
        this.quizDataService = quizDataService;
        this.dispatcher = dispatcher;
    }

    @NotificationListener
    public void onQuizPublished(QuizPublishedEvent event) {
        Optional<Quiz> found = quizDataService.getById(event.quizId());
        if (found.isEmpty() || !found.get().isVisible()) {
            logger.debug("Quiz {} no longer exists or is hidden again, skipping notifications", event.quizId());
            return;
        }
        Quiz quiz = found.get();

        Map<String, Object> payload = new HashMap<>();
        payload.put("quizName", quiz.getName());
        payload.put("subjectName", quiz.getSubject().getName());
        if (quiz.getSubjectUnit() != null)
            payload.put("subjectUnitName", quiz.getSubjectUnit().getName());

        dispatcher.dispatch(new NotificationRequest(NotificationType.QUIZ_PUBLISHED, quiz.getId(),
                quiz.getSubject().getId(), payload, Audience.all(), event.actorExternalId(), Set.of()));
    }
}
