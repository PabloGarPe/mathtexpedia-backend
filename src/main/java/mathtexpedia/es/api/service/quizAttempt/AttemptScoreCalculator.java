package mathtexpedia.es.api.service.quizAttempt;

public class AttemptScoreCalculator {

    private static final double DECAY_BASE = 0.9;
    private static final double MIN_FACTOR = 0.5;

    private AttemptScoreCalculator() {}

    public static double adjustedScore(double rawScore, int attemptNumber) {
        double factor = Math.max(MIN_FACTOR, Math.pow(DECAY_BASE, (double) attemptNumber - 1));
        return rawScore * factor;
    }
}
