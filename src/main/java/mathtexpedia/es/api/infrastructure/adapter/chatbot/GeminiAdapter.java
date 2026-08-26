package mathtexpedia.es.api.infrastructure.adapter.chatbot;

import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.port.chatbot.GenerativeAiPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Component
public class GeminiAdapter implements GenerativeAiPort {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAdapter.class);
    private static final int MAX_RETRIES = 3;
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}";
    private final RestClient restClient = RestClient.create();
    private final GeminiKeyManager keyManager;

    @Value("${gemini.model}")
    private String model;

    public GeminiAdapter(GeminiKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @Override
    public String generate(String prompt) throws PortActionNotPerformedException {
        Exception lastError = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String apiKey = keyManager.getCurrentKey();

            try {
                JsonNode response = restClient.post()
                        .uri(ENDPOINT, model, apiKey)
                        .body(Map.of("contents", List.of(
                                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
                        )))
                        .retrieve()
                        .body(JsonNode.class);

                keyManager.markCurrentKeyAsSuccess();
                return extractText(response);

            } catch (HttpStatusCodeException e) {
                lastError = e;
                int statusCode = e.getStatusCode().value();
                logger.error("Gemini API call failed (attempt {}/{}): {}", attempt + 1, MAX_RETRIES, e.getMessage());
                keyManager.markCurrentKeyAsFailed(statusCode);

                if (statusCode != 429 && attempt < MAX_RETRIES - 1) {
                    sleep(1000L * (attempt + 1));
                }
            } catch (Exception e) {
                lastError = e;
                logger.error("Gemini API call failed (attempt {}/{}): {}", attempt + 1, MAX_RETRIES, e.getMessage());
                keyManager.markCurrentKeyAsFailed(null);
            }
        }

        throw new PortActionNotPerformedException(
                "No se pudo obtener respuesta de Gemini tras " + MAX_RETRIES + " intentos: "
                        + (lastError != null ? lastError.getMessage() : "error desconocido"));
    }

    private String extractText(JsonNode response) {
        return response.path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText("");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
