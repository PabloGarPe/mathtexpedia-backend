package mathtexpedia.es.api.persistence.attemptAnswer;

import jakarta.persistence.*;
import lombok.Data;
import mathtexpedia.es.api.persistence.option.Option;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttempt;

@Entity
@Table(name = "attempt_answer")
@Data
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option selectedOption;

    private boolean isCorrect;
}
