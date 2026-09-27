package mathtexpedia.es.api.domain.model.quizAttempt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntryDto {

    private Long userId;
    private String username;
    private Double bestAdjustedScore;
    private int rank;

}
