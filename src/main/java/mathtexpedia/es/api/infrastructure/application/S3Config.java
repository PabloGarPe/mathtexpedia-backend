package mathtexpedia.es.api.infrastructure.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    private final Region region;
    private final String endpoint;

    public S3Config(@Value("${aws.s3.region}") String region,
                    @Value("${aws.s3.endpoint:}") String endpoint) {
        this.region = Region.of(region);
        this.endpoint = endpoint;
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .serviceConfiguration(s3Configuration());

        if (hasCustomEndpoint())
            builder.endpointOverride(URI.create(endpoint));

        return builder.build();
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
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
