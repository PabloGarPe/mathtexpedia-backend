package mathtexpedia.es.api.persistence.quizAttempt;

import jakarta.persistence.*;
import lombok.Data;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "quiz_attempt")
@Data
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private double score;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;
}
