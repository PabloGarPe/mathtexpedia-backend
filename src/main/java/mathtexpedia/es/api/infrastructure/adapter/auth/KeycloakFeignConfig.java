package mathtexpedia.es.api.infrastructure.adapter.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakFeignConfig {

    @Value("${app.auth.keycloak.base-url}")
    private String baseUrl;

    @Bean
    public KeycloakTokenClient keycloakTokenClient() {
        ObjectMapper objectMapper = new ObjectMapper();
        return Feign.builder()
                .client(new ApacheHttpClient())
                .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
                .decoder(new JacksonDecoder(objectMapper))
                .target(KeycloakTokenClient.class, baseUrl);
    }

    @Bean
    public KeycloakAdminClient keycloakAdminClient() {
        ObjectMapper objectMapper = new ObjectMapper();

        return Feign.builder()
                .client(new ApacheHttpClient())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .target(KeycloakAdminClient.class, baseUrl);
    }
}
