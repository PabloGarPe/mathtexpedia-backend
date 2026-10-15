package mathtexpedia.es.api.infrastructure.adapter.avatar;

import mathtexpedia.es.api.domain.port.avatar.AvatarStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;

/**
 * Fotos de perfil en el mismo bucket que los PDFs (bajo otro prefijo). Como allí, solo se accede desde el back:
 * no se generan URLs prefirmadas. Son imágenes pequeñas, así que se manejan en memoria.
 */
@Component
public class AvatarStorageAdapter implements AvatarStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public AvatarStorageAdapter(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public void upload(String key, byte[] content, String contentType) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(put, RequestBody.fromBytes(content));
    }

    @Override
    public Optional<byte[]> download(String key) {
        try {
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(b -> b.bucket(bucket).key(key));
            return Optional.of(bytes.asByteArray());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    }
}
