package mathtexpedia.es.api.infrastructure.adapter.chatbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GeminiKeyManager {

    private static final long RATE_LIMIT_COOLDOWN_SECONDS = 60;

    private final List<String> keys;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final Map<String, Instant> cooldownUntil = new ConcurrentHashMap<>();

    public GeminiKeyManager(@Value("${gemini.api.keys}") String rawKeys) {
        this.keys = Arrays.stream(rawKeys.split(","))
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .toList();

        if (keys.isEmpty()) {
            throw new IllegalStateException("No se ha configurado ninguna clave en GEMINI_API_KEYS");
        }
    }

    public synchronized String getCurrentKey() {
        for (int attempts = 0; attempts < keys.size(); attempts++) {
            String candidate = keys.get(currentIndex.get() % keys.size());
            Instant cooldown = cooldownUntil.get(candidate);
            if(cooldown == null || cooldown.isBefore(Instant.now())) {
                return candidate;
            }
            currentIndex.incrementAndGet();
        }
        return keys.get(currentIndex.get() % keys.size());
    }

    public synchronized void markCurrentKeyAsFailed(Integer statusCode) {
        String failedKey = keys.get(currentIndex.get() % keys.size());
        if (statusCode != null && statusCode == 429) {
            cooldownUntil.put(failedKey, Instant.now().plusSeconds(RATE_LIMIT_COOLDOWN_SECONDS));
        }
        currentIndex.incrementAndGet();
    }

    public synchronized void markCurrentKeyAsSuccess() {
        //NO-OP temporalmente
    }
}
