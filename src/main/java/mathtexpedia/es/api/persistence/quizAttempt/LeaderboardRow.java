package mathtexpedia.es.api.persistence.quizAttempt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardRow {
    private Long userId;
    private String username;
    private Double bestAdjustedScore;
}
