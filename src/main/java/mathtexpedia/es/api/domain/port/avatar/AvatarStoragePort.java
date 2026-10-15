package mathtexpedia.es.api.domain.port.avatar;

import java.util.Optional;

public interface AvatarStoragePort {

    /** Sube (o sobrescribe) el objeto con esa clave. */
    void upload(String key, byte[] content, String contentType);

    /** Devuelve el contenido, o vacío si la clave no existe en el bucket. */
    Optional<byte[]> download(String key);

    /** Borra el objeto. No falla si no existe. */
    void delete(String key);
}
