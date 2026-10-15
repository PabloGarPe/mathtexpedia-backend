package mathtexpedia.es.api.service.quizAttempt;

/**
 * Pondera la puntuación de un intento según el número de intento del usuario en ese cuestionario,
 * de forma que repetir el cuestionario hasta acertarlo entero no equivalga a hacerlo bien a la primera.
 *
 * <p>Decaimiento geométrico con suelo: el primer intento vale el 100 %, y cada intento posterior
 * conserva solo {@value #DECAY_BASE} del anterior (castiga mucho los primeros reintentos y se suaviza
 * después). El suelo evita que un intento acabe valiendo 0 y permite seguir ordenando por puntuación.
 *
 * <pre>
 * intento   1      2     3      4      5      6+
 * factor    1.00   0.75  0.5625 0.422  0.316  0.25 (suelo)
 * un 10 →   10.0   7.5   5.63   4.22   3.16   2.5
 * </pre>
 */
public class AttemptScoreCalculator {

    private static final double DECAY_BASE = 0.75;
    private static final double MIN_FACTOR = 0.25;

    private AttemptScoreCalculator() {}

    public static double adjustedScore(double rawScore, int attemptNumber) {
        double factor = Math.max(MIN_FACTOR, Math.pow(DECAY_BASE, (double) attemptNumber - 1));
        return rawScore * factor;
    }
}
