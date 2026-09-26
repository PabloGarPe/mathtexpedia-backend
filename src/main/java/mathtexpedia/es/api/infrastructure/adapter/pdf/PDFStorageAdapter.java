package mathtexpedia.es.api.infrastructure.adapter.pdf;

import mathtexpedia.es.api.domain.model.pdf.PDFContent;
import mathtexpedia.es.api.domain.port.pdf.PDFStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Optional;

/**
 * Acceso a S3 exclusivamente desde el back. No se generan URLs prefirmadas:
 * el bucket puede (y debe) tener bloqueado todo acceso público.
 */
@Component
public class PDFStorageAdapter implements PDFStoragePort {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final S3Client s3Client;
    private final String bucket;

    public PDFStorageAdapter(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public void upload(String key, InputStream content, long size) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(PDF_CONTENT_TYPE)
                .build();

        s3Client.putObject(put, RequestBody.fromInputStream(content, size));
    }

    @Override
    public Optional<PDFContent> download(String key) {
        try {
            ResponseInputStream<GetObjectResponse> stream =
                    s3Client.getObject(b -> b.bucket(bucket).key(key));
            return Optional.of(new PDFContent(stream, stream.response().contentLength()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    }
}