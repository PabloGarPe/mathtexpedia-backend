package mathtexpedia.es.api.infrastructure.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    private final Region region;
    private final String endpoint;
    private final AwsCredentialsProvider credentialsProvider;

    /**
     * AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY llegan aquí vía {@code .env} (cargado por
     * {@code ApiApplication} como propiedades de sistema de Java) o como variables de entorno
     * reales del contenedor en producción; en ambos casos Spring las resuelve igual.
     *
     * <p>El SDK de AWS no lee esas propiedades de Spring: {@code DefaultCredentialsProvider}
     * solo busca la variable de entorno del SO o la propiedad de sistema {@code aws.accessKeyId},
     * no la de Spring. Por eso se construyen las credenciales explícitamente aquí cuando están
     * disponibles, y solo se cae a {@code DefaultCredentialsProvider} (rol de IAM, variables de
     * entorno reales, etc.) si no se han definido.
     */
    public S3Config(@Value("${aws.s3.region}") String region,
                    @Value("${aws.s3.endpoint:}") String endpoint,
                    @Value("${AWS_ACCESS_KEY_ID:}") String accessKeyId,
                    @Value("${AWS_SECRET_ACCESS_KEY:}") String secretAccessKey) {
        this.region = Region.of(region);
        this.endpoint = endpoint;
        this.credentialsProvider = buildCredentialsProvider(accessKeyId, secretAccessKey);
    }

    private static AwsCredentialsProvider buildCredentialsProvider(String accessKeyId, String secretAccessKey) {
        if (accessKeyId != null && !accessKeyId.isBlank() && secretAccessKey != null && !secretAccessKey.isBlank())
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));

        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(s3Configuration());

        if (hasCustomEndpoint())
            builder.endpointOverride(URI.create(endpoint));

        return builder.build();
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(s3Configuration());

        if (hasCustomEndpoint())
            builder.endpointOverride(URI.create(endpoint));

        return builder.build();
    }

    private S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(hasCustomEndpoint())
                .build();
    }

    private boolean hasCustomEndpoint() {
        return endpoint != null && !endpoint.isBlank();
    }
}
