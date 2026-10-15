package mathtexpedia.es.api.persistence.quizAttempt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LeaderboardRow {
    private Long userId;
    private String username;
    private Instant avatarUpdatedAt;
    private Double bestAdjustedScore;
}
