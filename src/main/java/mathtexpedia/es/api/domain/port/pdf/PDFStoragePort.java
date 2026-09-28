package mathtexpedia.es.api.domain.port.pdf;

import mathtexpedia.es.api.domain.model.pdf.PDFContent;

import java.io.InputStream;
import java.util.Optional;

public interface PDFStoragePort {

    /** Sube (o sobrescribe) el objeto con esa clave. */
    void upload(String key, InputStream content, long size);

    /** Devuelve el contenido, o vacío si la clave no existe en el bucket. */
    Optional<PDFContent> download(String key);

    /** Borra el objeto. No falla si no existe. */
    void delete(String key);
}