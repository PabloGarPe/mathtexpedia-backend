package mathtexpedia.es.api.domain.model.pdf;

/** Criterio de ordenación de los listados de PDFs. */
public enum PdfSort {
    /** Mejor valorados primero: puntuación (límite inferior de Wilson), luego 👍, vistas e id. */
    RATING,
    /** Más vistos primero. */
    VIEWS,
    /** Editados más recientemente primero (los que no tienen fecha, al final). */
    RECENT,
    /** Alfabético por nombre, sin distinguir mayúsculas ni acentos. */
    NAME
}
