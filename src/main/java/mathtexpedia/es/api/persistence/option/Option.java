package mathtexpedia.es.api.persistence.option;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import mathtexpedia.es.api.persistence.question.Question;

@Entity
@Data
@Table(name = "option")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String text;

    private boolean isCorrect;

    private int position;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
