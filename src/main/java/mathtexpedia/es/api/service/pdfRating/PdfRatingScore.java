package mathtexpedia.es.api.service.pdfRating;

/** Puntuación para ordenar PDFs por valoración. */
public final class PdfRatingScore {

    /** z² para un 95 % de confianza (z = 1.96). */
    private static final double Z_SQUARED = 3.8416;
    private static final double Z = 1.96;

    private PdfRatingScore() {
    }

    /**
     * Límite inferior del intervalo de Wilson de la proporción de 👍 (0..1): pondera la proporción por la cantidad de votos,
     * de modo que 3 de 3 vale menos que 90 de 100. 0 si no hay votos o ninguno es 👍. Misma fórmula que la consulta de mejor valorados.
     */
    public static double wilson(long likes, long dislikes) {
        long n = likes + dislikes;
        if (n <= 0 || likes <= 0)
            return 0.0;

        double p = (double) likes / n;
        double score = (p + Z_SQUARED / (2 * n) - Z * Math.sqrt((p * (1 - p) + Z_SQUARED / (4.0 * n)) / n))
                / (1 + Z_SQUARED / n);
        return Math.max(0.0, score);
    }
}
